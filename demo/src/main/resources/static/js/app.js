/**
 * Portfolio Manager - Main Application JavaScript
 * Handles API interactions, UI updates, and state management
 */

// API Configuration
const API_BASE_URL = '/api';

// DOM Elements
const elements = {
    // Summary cards
    totalValue: document.getElementById('total-value'),
    totalCost: document.getElementById('total-cost'),
    totalGainLoss: document.getElementById('total-gain-loss'),
    gainLossPercent: document.getElementById('gain-loss-percent'),
    totalAssets: document.getElementById('total-assets'),

    // Table
    assetsTbody: document.getElementById('assets-tbody'),
    assetFilter: document.getElementById('asset-filter'),

    // Top performers
    topGainers: document.getElementById('top-gainers'),
    topLosers: document.getElementById('top-losers'),

    // Modals
    assetModal: document.getElementById('asset-modal'),
    deleteModal: document.getElementById('delete-modal'),
    modalTitle: document.getElementById('modal-title'),

    // Form elements
    assetForm: document.getElementById('asset-form'),
    assetId: document.getElementById('asset-id'),
    assetSymbol: document.getElementById('asset-symbol'),
    assetName: document.getElementById('asset-name'),
    assetType: document.getElementById('asset-type'),
    assetQuantity: document.getElementById('asset-quantity'),
    assetBuyPrice: document.getElementById('asset-buy-price'),
    assetPurchaseDate: document.getElementById('asset-purchase-date'),

    // Delete modal
    deleteAssetName: document.getElementById('delete-asset-name'),

    // Buttons
    addAssetBtn: document.getElementById('add-asset-btn'),
    modalCloseBtn: document.getElementById('modal-close'),
    cancelBtn: document.getElementById('cancel-btn'),
    submitBtn: document.getElementById('submit-btn'),
    deleteModalClose: document.getElementById('delete-modal-close'),
    deleteCancelBtn: document.getElementById('delete-cancel-btn'),
    deleteConfirmBtn: document.getElementById('delete-confirm-btn'),

    // Toast
    toast: document.getElementById('toast'),
    toastMessage: document.getElementById('toast-message'),

    // News elements
    marketNews: document.getElementById('market-news'),
    newsTabs: document.querySelectorAll('.news-tab'),
    stockNewsModal: document.getElementById('stock-news-modal'),
    stockNewsSymbol: document.getElementById('stock-news-symbol'),
    stockNewsContainer: document.getElementById('stock-news-container'),
    stockNewsModalClose: document.getElementById('stock-news-modal-close')
};

// State
let currentDeleteId = null;
let allAssets = [];
let currentSortColumn = null;
let currentSortDirection = 'asc';

// ===================================
// API Service
// ===================================
const api = {
    async get(endpoint) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`);
        return response.json();
    },

    async post(endpoint, data) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return response.json();
    },

    async put(endpoint, data) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return response.json();
    },

    async delete(endpoint) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'DELETE'
        });
        return response.json();
    }
};

// ===================================
// Utility Functions
// ===================================
function formatCurrency(value) {
    if (value === null || value === undefined) return '$0.00';
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(value);
}

function formatPercent(value) {
    if (value === null || value === undefined) return '0.00%';
    const sign = value >= 0 ? '+' : '';
    return `${sign}${Number(value).toFixed(2)}%`;
}

function formatNumber(value, decimals = 4) {
    if (value === null || value === undefined) return '0';
    return Number(value).toLocaleString('en-US', {
        minimumFractionDigits: 0,
        maximumFractionDigits: decimals
    });
}

function showToast(message, type = 'success') {
    elements.toast.className = `toast ${type}`;
    elements.toastMessage.textContent = message;
    elements.toast.classList.remove('hidden');

    setTimeout(() => {
        elements.toast.classList.add('hidden');
    }, 3000);
}

// ===================================
// UI Update Functions
// ===================================
function updateSummaryCards(summary) {
    elements.totalValue.textContent = formatCurrency(summary.totalValue);
    elements.totalCost.textContent = formatCurrency(summary.totalCostBasis);
    elements.totalGainLoss.textContent = formatCurrency(summary.totalGainLoss);

    const percentClass = summary.totalGainLoss >= 0 ? 'positive' : 'negative';
    elements.gainLossPercent.textContent = `(${formatPercent(summary.totalGainLossPercentage)})`;
    elements.gainLossPercent.className = `card-percent ${percentClass}`;

    // Update gain-loss card border color based on value
    const gainLossCard = document.querySelector('.summary-card.gain-loss');
    if (summary.totalGainLoss >= 0) {
        gainLossCard.style.borderLeftColor = 'var(--color-success)';
    } else {
        gainLossCard.style.borderLeftColor = 'var(--color-danger)';
    }

    elements.totalAssets.textContent = summary.totalAssets || 0;
}

function updateTopPerformers(summary) {
    // Top Gainers
    if (summary.topGainers && summary.topGainers.length > 0) {
        elements.topGainers.innerHTML = summary.topGainers.map(asset => `
            <div class="performer-item">
                <div class="performer-info">
                    <span class="performer-symbol">${asset.symbol}</span>
                    <span class="performer-name">${truncate(asset.name, 25)}</span>
                </div>
                <span class="performer-change positive">${formatPercent(asset.gainLossPercentage)}</span>
            </div>
        `).join('');
    } else {
        elements.topGainers.innerHTML = '<div class="empty-state">No gainers yet</div>';
    }

    // Top Losers
    if (summary.topLosers && summary.topLosers.length > 0) {
        elements.topLosers.innerHTML = summary.topLosers.map(asset => `
            <div class="performer-item">
                <div class="performer-info">
                    <span class="performer-symbol">${asset.symbol}</span>
                    <span class="performer-name">${truncate(asset.name, 25)}</span>
                </div>
                <span class="performer-change negative">${formatPercent(asset.gainLossPercentage)}</span>
            </div>
        `).join('');
    } else {
        elements.topLosers.innerHTML = '<div class="empty-state">No losers yet</div>';
    }
}

function truncate(str, maxLength) {
    if (!str) return '';
    return str.length > maxLength ? str.substring(0, maxLength) + '...' : str;
}

function renderAssetsTable(assets) {
    if (!assets || assets.length === 0) {
        elements.assetsTbody.innerHTML = `
            <tr class="loading-row">
                <td colspan="9">
                    <span>No assets found. Add your first asset!</span>
                </td>
            </tr>
        `;
        return;
    }

    elements.assetsTbody.innerHTML = assets.map(asset => {
        const gainLossClass = asset.gainLoss >= 0 ? 'positive' : 'negative';

        return `
            <tr data-id="${asset.id}">
                <td class="symbol-cell" onclick="openStockNews('${asset.symbol}')" title="Click for ${asset.symbol} news">${asset.symbol}</td>
                <td>${truncate(asset.name, 30)}</td>
                <td><span class="type-badge ${asset.type}">${formatAssetType(asset.type)}</span></td>
                <td>${formatNumber(asset.quantity)}</td>
                <td>${formatCurrency(asset.buyPrice)}</td>
                <td>${formatCurrency(asset.currentPrice)}</td>
                <td>${formatCurrency(asset.currentValue)}</td>
                <td>
                    <div class="gain-loss-cell ${gainLossClass}">
                        <span class="gain-loss-value">${formatCurrency(asset.gainLoss)}</span>
                        <span class="gain-loss-percent">${formatPercent(asset.gainLossPercentage)}</span>
                    </div>
                </td>
                <td>
                    <div class="actions-cell">
                        <button class="btn btn-icon edit" onclick="editAsset(${asset.id})" title="Edit">
                            ✏️
                        </button>
                        <button class="btn btn-icon delete" onclick="confirmDelete(${asset.id}, '${asset.name.replace(/'/g, "\\'")}')" title="Delete">
                            🗑️
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function formatAssetType(type) {
    const typeMap = {
        'STOCK': 'Stock',
        'BOND': 'Bond',
        'ETF': 'ETF',
        'MUTUAL_FUND': 'Mutual Fund',
        'CRYPTO': 'Crypto',
        'REAL_ESTATE': 'Real Estate',
        'CASH': 'Cash'
    };
    return typeMap[type] || type;
}

// ===================================
// Sorting Functions
// ===================================
function sortAssets(column) {
    // Toggle direction if same column, else start ascending
    if (currentSortColumn === column) {
        currentSortDirection = currentSortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        currentSortColumn = column;
        currentSortDirection = 'asc';
    }

    // Update header indicators
    document.querySelectorAll('.assets-table th.sortable').forEach(th => {
        th.classList.remove('asc', 'desc');
        if (th.dataset.sort === column) {
            th.classList.add(currentSortDirection);
        }
    });

    // Sort the assets
    const sortedAssets = [...allAssets].sort((a, b) => {
        let aVal = a[column];
        let bVal = b[column];

        // Handle null/undefined
        if (aVal == null) aVal = '';
        if (bVal == null) bVal = '';

        // String comparison for text fields
        if (typeof aVal === 'string' && typeof bVal === 'string') {
            const comparison = aVal.localeCompare(bVal);
            return currentSortDirection === 'asc' ? comparison : -comparison;
        }

        // Numeric comparison
        const numA = parseFloat(aVal) || 0;
        const numB = parseFloat(bVal) || 0;

        // For gainLoss, always prefer gains (higher is better)
        // asc = best performers first (highest gain)
        // desc = worst performers first (biggest loss)
        if (column === 'gainLoss' || column === 'gainLossPercentage') {
            return currentSortDirection === 'asc' ? numB - numA : numA - numB;
        }

        return currentSortDirection === 'asc' ? numA - numB : numB - numA;
    });

    renderAssetsTable(sortedAssets);
}

// ===================================
// Modal Functions
// ===================================
function openAddModal() {
    elements.modalTitle.textContent = 'Add New Asset';
    elements.assetForm.reset();
    elements.assetId.value = '';
    elements.assetModal.classList.remove('hidden');
}

function openEditModal(asset) {
    elements.modalTitle.textContent = 'Edit Asset';
    elements.assetId.value = asset.id;
    elements.assetSymbol.value = asset.symbol;
    elements.assetName.value = asset.name;
    elements.assetType.value = asset.type;
    elements.assetQuantity.value = asset.quantity;
    elements.assetBuyPrice.value = asset.buyPrice;
    elements.assetPurchaseDate.value = asset.purchaseDate || '';
    elements.assetModal.classList.remove('hidden');
}

function closeModal() {
    elements.assetModal.classList.add('hidden');
    elements.assetForm.reset();
}

function openDeleteModal(id, name) {
    currentDeleteId = id;
    elements.deleteAssetName.textContent = name;
    elements.deleteModal.classList.remove('hidden');
}

function closeDeleteModal() {
    elements.deleteModal.classList.add('hidden');
    currentDeleteId = null;
}

// ===================================
// Data Functions
// ===================================
async function loadPortfolioData() {
    try {
        const response = await api.get('/portfolio/summary');
        if (response.success && response.data) {
            const summary = response.data;
            allAssets = summary.assets || [];

            updateSummaryCards(summary);
            updateTopPerformers(summary);
            renderAssetsTable(allAssets);

            // Update charts (defined in charts.js)
            if (typeof updateCharts === 'function') {
                updateCharts(summary);
            }
        }
    } catch (error) {
        console.error('Error loading portfolio data:', error);
        showToast('Failed to load portfolio data', 'error');
    }
}

async function createAsset(assetData) {
    try {
        const response = await api.post('/assets', assetData);
        if (response.success) {
            showToast('Asset added successfully!', 'success');
            closeModal();
            loadPortfolioData();
        } else {
            showToast(response.message || 'Failed to add asset', 'error');
        }
    } catch (error) {
        console.error('Error creating asset:', error);
        showToast('Failed to add asset', 'error');
    }
}

async function updateAsset(id, assetData) {
    try {
        const response = await api.put(`/assets/${id}`, assetData);
        if (response.success) {
            showToast('Asset updated successfully!', 'success');
            closeModal();
            loadPortfolioData();
        } else {
            showToast(response.message || 'Failed to update asset', 'error');
        }
    } catch (error) {
        console.error('Error updating asset:', error);
        showToast('Failed to update asset', 'error');
    }
}

async function deleteAsset(id) {
    try {
        const response = await api.delete(`/assets/${id}`);
        if (response.success) {
            showToast('Asset deleted successfully!', 'success');
            closeDeleteModal();
            loadPortfolioData();
        } else {
            showToast(response.message || 'Failed to delete asset', 'error');
        }
    } catch (error) {
        console.error('Error deleting asset:', error);
        showToast('Failed to delete asset', 'error');
    }
}

// ===================================
// Global Functions (for onclick handlers)
// ===================================
window.editAsset = function (id) {
    const asset = allAssets.find(a => a.id === id);
    if (asset) {
        openEditModal(asset);
    }
};

window.confirmDelete = function (id, name) {
    openDeleteModal(id, name);
};

// ===================================
// Event Listeners
// ===================================
document.addEventListener('DOMContentLoaded', () => {
    // Load initial data
    loadPortfolioData();

    // Add Asset button
    elements.addAssetBtn.addEventListener('click', openAddModal);

    // Modal close buttons
    elements.modalCloseBtn.addEventListener('click', closeModal);
    elements.cancelBtn.addEventListener('click', closeModal);
    elements.assetModal.querySelector('.modal-backdrop').addEventListener('click', closeModal);

    // Delete modal close buttons
    elements.deleteModalClose.addEventListener('click', closeDeleteModal);
    elements.deleteCancelBtn.addEventListener('click', closeDeleteModal);
    elements.deleteModal.querySelector('.modal-backdrop').addEventListener('click', closeDeleteModal);

    // Delete confirm
    elements.deleteConfirmBtn.addEventListener('click', () => {
        if (currentDeleteId) {
            deleteAsset(currentDeleteId);
        }
    });

    // Form submission
    elements.assetForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const assetData = {
            symbol: elements.assetSymbol.value.trim(),
            name: elements.assetName.value.trim(),
            type: elements.assetType.value,
            quantity: parseFloat(elements.assetQuantity.value),
            buyPrice: parseFloat(elements.assetBuyPrice.value),
            purchaseDate: elements.assetPurchaseDate.value || null
        };

        const id = elements.assetId.value;
        if (id) {
            updateAsset(id, assetData);
        } else {
            createAsset(assetData);
        }
    });

    // Asset type filter
    elements.assetFilter.addEventListener('change', (e) => {
        const filterType = e.target.value;
        if (filterType) {
            const filtered = allAssets.filter(a => a.type === filterType);
            renderAssetsTable(filtered);
        } else {
            renderAssetsTable(allAssets);
        }
    });

    // Sortable table headers
    document.querySelectorAll('.assets-table th.sortable').forEach(th => {
        th.addEventListener('click', () => {
            sortAssets(th.dataset.sort);
        });
    });

    // CSV upload controls
    const csvFileInput = document.getElementById('csv-file-input');
    const uploadCsvBtn = document.getElementById('upload-csv-btn');

    // Clicking the upload button triggers the file chooser
    uploadCsvBtn.addEventListener('click', () => csvFileInput.click());

    // When a file is selected, perform upload
    csvFileInput.addEventListener('change', async (e) => {
        const file = e.target.files && e.target.files[0];
        if (!file) return;

        // Basic client-side validation
        if (!file.name.toLowerCase().endsWith('.csv')) {
            showToast('Please select a CSV file', 'error');
            return;
        }

        try {
            uploadCsvBtn.disabled = true;
            uploadCsvBtn.textContent = 'Uploading...';

            const formData = new FormData();
            formData.append('file', file);

            const resp = await fetch(`${API_BASE_URL}/assets/upload`, {
                method: 'POST',
                body: formData
            });

            const result = await resp.json();
            if (resp.ok && result && result.success) {
                showToast(result.message || 'CSV imported successfully', 'success');
                // refresh portfolio
                loadPortfolioData();
            } else {
                showToast((result && result.message) || 'Failed to import CSV', 'error');
            }
        } catch (err) {
            console.error('CSV upload failed', err);
            showToast('CSV upload failed', 'error');
        } finally {
            uploadCsvBtn.disabled = false;
            uploadCsvBtn.textContent = 'Upload CSV';
            csvFileInput.value = null; // reset input
        }
    });

    // Keyboard shortcuts
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeModal();
            closeDeleteModal();
            closeStockNewsModal();
        }
    });

    // ===================================
    // News Event Listeners
    // ===================================

    // Load market news on page load
    loadMarketNews('crypto');

    // News tabs
    elements.newsTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            elements.newsTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            loadMarketNews(tab.dataset.category);
        });
    });

    // Stock news modal close
    if (elements.stockNewsModalClose) {
        elements.stockNewsModalClose.addEventListener('click', closeStockNewsModal);
    }
    if (elements.stockNewsModal) {
        elements.stockNewsModal.querySelector('.modal-backdrop').addEventListener('click', closeStockNewsModal);
    }
});

// ===================================
// News Functions
// ===================================

async function loadMarketNews(category = 'general') {
    if (!elements.marketNews) return;

    elements.marketNews.innerHTML = `
        <div class="news-loading">
            <div class="loading-spinner"></div>
            <span>Loading ${category} news...</span>
        </div>
    `;

    try {
        const response = await api.get(`/news/market?category=${category}`);
        if (response.success && response.data && response.data.length > 0) {
            renderMarketNews(response.data);
        } else {
            elements.marketNews.innerHTML = '<div class="news-empty">No news available at the moment.</div>';
        }
    } catch (error) {
        console.error('Error loading market news:', error);
        elements.marketNews.innerHTML = '<div class="news-empty">Failed to load news. Please try again later.</div>';
    }
}

function renderMarketNews(newsItems) {
    elements.marketNews.innerHTML = newsItems.slice(0, 10).map(news => `
        <a href="${news.url}" target="_blank" rel="noopener noreferrer" class="news-card">
            ${news.image ?
            `<img src="${news.image}" alt="" class="news-card-image" onerror="this.classList.add('placeholder'); this.outerHTML='<div class=\\'news-card-image placeholder\\'>📰</div>'">` :
            '<div class="news-card-image placeholder">📰</div>'
        }
            <div class="news-card-content">
                <div class="news-card-headline">${escapeHtml(news.headline)}</div>
                <div class="news-card-meta">
                    <span class="news-card-source">${escapeHtml(news.source || 'News')}</span>
                    <span class="news-card-time">${formatTimeAgo(news.datetime)}</span>
                </div>
            </div>
        </a>
    `).join('');
}

async function loadStockNews(symbol) {
    if (!elements.stockNewsContainer) return;

    elements.stockNewsContainer.innerHTML = `
        <div class="news-loading">
            <div class="loading-spinner"></div>
            <span>Loading ${symbol} news...</span>
        </div>
    `;

    try {
        const response = await api.get(`/news/company/${symbol}`);
        if (response.success && response.data && response.data.length > 0) {
            renderStockNews(response.data);
        } else {
            elements.stockNewsContainer.innerHTML = `
                <div class="news-empty">No recent news found for ${symbol}.</div>
            `;
        }
    } catch (error) {
        console.error('Error loading stock news:', error);
        elements.stockNewsContainer.innerHTML = `
            <div class="news-empty">Failed to load news for ${symbol}. Please try again later.</div>
        `;
    }
}

function renderStockNews(newsItems) {
    elements.stockNewsContainer.innerHTML = newsItems.slice(0, 15).map(news => `
        <a href="${news.url}" target="_blank" rel="noopener noreferrer" class="stock-news-item">
            ${news.image ?
            `<img src="${news.image}" alt="" class="stock-news-thumb" onerror="this.style.display='none'">` :
            ''
        }
            <div class="stock-news-info">
                <div class="stock-news-headline">${escapeHtml(news.headline)}</div>
                ${news.summary ? `<div class="stock-news-summary">${escapeHtml(truncate(news.summary, 150))}</div>` : ''}
                <div class="stock-news-meta">
                    <span class="stock-news-source">${escapeHtml(news.source || 'News')}</span>
                    <span class="stock-news-time">${formatTimeAgo(news.datetime)}</span>
                </div>
            </div>
        </a>
    `).join('');
}

function openStockNewsModal(symbol) {
    if (!elements.stockNewsModal) return;
    elements.stockNewsSymbol.textContent = symbol;
    elements.stockNewsModal.classList.remove('hidden');
    loadStockNews(symbol);
}

function closeStockNewsModal() {
    if (elements.stockNewsModal) {
        elements.stockNewsModal.classList.add('hidden');
    }
}

// Global function for onclick handler
window.openStockNews = function (symbol) {
    openStockNewsModal(symbol);
};

function formatTimeAgo(timestamp) {
    if (!timestamp) return '';

    const seconds = Math.floor((Date.now() / 1000) - timestamp);

    if (seconds < 60) return 'just now';
    if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
    if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`;
    if (seconds < 604800) return `${Math.floor(seconds / 86400)}d ago`;

    return new Date(timestamp * 1000).toLocaleDateString();
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
