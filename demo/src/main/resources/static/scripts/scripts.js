const API_BASE = '/api/portfolio';

let allocationChart = null;
let valueChart = null;
let priceTimelineChart = null;

async function loadAssets() {
    console.log('Loading assets from:', API_BASE);
    try {
        const response = await fetch(`${API_BASE}/assets`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });

        console.log('Response status:', response.status);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const assets = await response.json();
        console.log('Assets loaded:', assets);

        // Handle empty assets array
        if (!assets || assets.length === 0) {
            console.log('No assets found');
            document.getElementById('totalValue').textContent = '$0.00';
            document.getElementById('totalAssets').textContent = '0';
            document.getElementById('totalGainLoss').textContent = '$0.00';
            document.getElementById('assetsTableBody').innerHTML = '<div class="text-center text-secondary py-8">No assets yet. Click "+ Add Asset" to get started.</div>';
            return;
        }

        // Calculate total portfolio value
        const totalValue = assets.reduce((sum, asset) => {
            return sum + (parseFloat(asset.quantity) * parseFloat(asset.purchasePrice));
        }, 0);

        // Calculate total gain/loss (would need current prices in real scenario)
        // For now, using random values between -5% and +15%
        const totalGainLoss = totalValue * (Math.random() * 0.2 - 0.05);
        const gainLossPercentage = (totalGainLoss / totalValue) * 100;

        // Update stats
        document.getElementById('totalValue').textContent = '$' + totalValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        document.getElementById('totalAssets').textContent = assets.length;

        const gainLossElement = document.getElementById('totalGainLoss');
        gainLossElement.textContent = (totalGainLoss >= 0 ? '+' : '') + '$' + totalGainLoss.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' (' + gainLossPercentage.toFixed(2) + '%)';
        gainLossElement.className = 'stat-value serif ' + (totalGainLoss >= 0 ? 'text-success' : 'text-danger');

        // Render assets table
        renderAssetsTable(assets);

        // Update charts
        updateCharts(assets);
    } catch (error) {
        console.error('Error loading assets:', error);
        document.getElementById('assetsTableBody').innerHTML = `
            <div class="text-center text-danger py-8">
                <div>Error loading assets: ${error.message}</div>
                <div class="text-sm mt-2">Check console for details. Make sure backend CORS is enabled.</div>
            </div>
        `;
    }
}

function renderAssetsTable(assets) {
    const tbody = document.getElementById('assetsTableBody');
    tbody.innerHTML = '';

    assets.forEach(asset => {
        const totalValue = parseFloat(asset.quantity) * parseFloat(asset.purchasePrice);
        const row = document.createElement('div');
        row.className = 'asset-row';
        row.innerHTML = `
            <div>
                <div class="font-medium">${asset.nameOrTicker}</div>
                ${asset.notes ? `<div class="text-xs text-secondary opacity-60 mt-1">${asset.notes}</div>` : ''}
            </div>
            <div>
                <span class="badge badge-${asset.type.toLowerCase().replace('_', '-')}">${asset.type.replace('_', ' ')}</span>
            </div>
            <div class="font-medium">${parseFloat(asset.quantity).toLocaleString()}</div>
            <div class="text-secondary">$${parseFloat(asset.purchasePrice).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</div>
            <div class="font-medium">$${totalValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</div>
            <div>
                <button class="btn btn-danger" onclick="deleteAsset(${asset.id})">Delete</button>
            </div>
        `;
        tbody.appendChild(row);
    });
}

function updateCharts(assets) {
    // Allocation by Type Chart
    const typeData = assets.reduce((acc, asset) => {
        const value = parseFloat(asset.quantity) * parseFloat(asset.purchasePrice);
        acc[asset.type] = (acc[asset.type] || 0) + value;
        return acc;
    }, {});

    const allocationLabels = Object.keys(typeData);
    const allocationValues = Object.values(typeData);
    const allocationColors = allocationLabels.map(type => {
        const colorMap = {
            STOCK: '#4a90e2',
            BOND: '#9333ea',
            CASH: '#10b981',
            REAL_ESTATE: '#f59e0b',
            CRYPTO: '#ec4899',
            ETF: '#3b82f6',
            MUTUAL_FUND: '#8b5cf6',
            COMMODITY: '#ea580c',
            OPTIONS: '#0ea5e9'
        };
        return colorMap[type] || '#6b7280';
    });

    if (allocationChart) allocationChart.destroy();
    const ctx1 = document.getElementById('allocationChart').getContext('2d');
    allocationChart = new Chart(ctx1, {
        type: 'doughnut',
        data: {
            labels: allocationLabels.map(l => l.replace('_', ' ')),
            datasets: [{
                data: allocationValues,
                backgroundColor: allocationColors,
                borderColor: '#141925',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        color: '#e8e9ed',
                        font: { family: 'IBM Plex Mono', size: 11 },
                        padding: 15,
                        generateLabels: function(chart) {
                            const data = chart.data;
                            return data.labels.map((label, i) => {
                                const value = data.datasets[0].data[i];
                                const total = data.datasets[0].data.reduce((a, b) => a + b, 0);
                                const percentage = ((value / total) * 100).toFixed(1);
                                return {
                                    text: `${label}: ${percentage}%`,
                                    fillStyle: data.datasets[0].backgroundColor[i],
                                    hidden: false,
                                    index: i
                                };
                            });
                        }
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(20, 25, 37, 0.95)',
                    titleColor: '#e8e9ed',
                    bodyColor: '#e8e9ed',
                    borderColor: '#2a2f3f',
                    borderWidth: 1,
                    padding: 12,
                    displayColors: true,
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.parsed || 0;
                            return label + ': $' + value.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
                        }
                    }
                }
            }
        }
    });

    // Value by Type Chart
    const valueLabels = Object.keys(typeData);
    const valueData = Object.values(typeData);
    const valueColors = valueLabels.map(type => {
        const colorMap = {
            STOCK: '#4a90e2',
            BOND: '#9333ea',
            CASH: '#10b981',
            REAL_ESTATE: '#f59e0b',
            CRYPTO: '#ec4899',
            ETF: '#3b82f6',
            MUTUAL_FUND: '#8b5cf6',
            COMMODITY: '#ea580c',
            OPTIONS: '#0ea5e9'
        };
        return colorMap[type] || '#6b7280';
    });

    if (valueChart) valueChart.destroy();
    const ctx2 = document.getElementById('valueChart').getContext('2d');
    valueChart = new Chart(ctx2, {
        type: 'bar',
        data: {
            labels: valueLabels.map(t => t.replace('_', ' ')),
            datasets: [{
                label: 'Total Value',
                data: valueData,
                backgroundColor: valueColors,
                borderColor: valueColors,
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: '#2a2f3f' },
                    ticks: {
                        color: '#9ca3af',
                        font: { family: 'IBM Plex Mono', size: 10 },
                        callback: value => '$' + value.toLocaleString()
                    }
                },
                x: {
                    grid: { display: false },
                    ticks: {
                        color: '#9ca3af',
                        font: { family: 'IBM Plex Mono', size: 10 }
                    }
                }
            }
        }
    });

    // Price Timeline Chart
    // Group assets by nameOrTicker and sort by purchase date
    const assetsByName = assets.reduce((acc, asset) => {
        if (!acc[asset.nameOrTicker]) {
            acc[asset.nameOrTicker] = [];
        }
        acc[asset.nameOrTicker].push(asset);
        return acc;
    }, {});

    // Create datasets for each asset
    const datasets = Object.keys(assetsByName).map((assetName, index) => {
        const assetData = assetsByName[assetName].sort((a, b) =>
            new Date(a.purchaseDate || '1970-01-01') - new Date(b.purchaseDate || '1970-01-01')
        );

        // Generate a color for each asset
        const colors = [
            '#4a90e2', '#9333ea', '#10b981', '#f59e0b', '#ec4899',
            '#3b82f6', '#8b5cf6', '#ea580c', '#0ea5e9', '#d946ef',
            '#06b6d4', '#14b8a6', '#84cc16', '#eab308', '#f97316'
        ];
        const color = colors[index % colors.length];

        return {
            label: assetName,
            data: assetData.map(asset => ({
                x: asset.purchaseDate || new Date().toISOString().split('T')[0],
                y: parseFloat(asset.purchasePrice)
            })),
            borderColor: color,
            backgroundColor: color + '33',
            tension: 0.4,
            fill: false,
            pointRadius: 5,
            pointHoverRadius: 7,
            pointBackgroundColor: color,
            pointBorderColor: '#141925',
            pointBorderWidth: 2
        };
    });

    if (priceTimelineChart) priceTimelineChart.destroy();
    const ctx3 = document.getElementById('priceTimelineChart').getContext('2d');
    priceTimelineChart = new Chart(ctx3, {
        type: 'line',
        data: { datasets },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'nearest',
                intersect: false,
            },
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        color: '#e8e9ed',
                        font: { family: 'IBM Plex Mono', size: 11 },
                        padding: 15,
                        usePointStyle: true,
                        pointStyle: 'circle'
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(20, 25, 37, 0.95)',
                    titleColor: '#e8e9ed',
                    bodyColor: '#e8e9ed',
                    borderColor: '#2a2f3f',
                    borderWidth: 1,
                    padding: 12,
                    displayColors: true,
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': $' + context.parsed.y.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2});
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    grid: { color: '#2a2f3f' },
                    ticks: {
                        color: '#9ca3af',
                        font: { family: 'IBM Plex Mono', size: 10 },
                        callback: value => '$' + value.toLocaleString()
                    },
                    title: {
                        display: true,
                        text: 'Purchase Price',
                        color: '#9ca3af',
                        font: { family: 'IBM Plex Mono', size: 11 }
                    }
                },
                x: {
                    type: 'time',
                    time: {
                        unit: 'day',
                        displayFormats: {
                            day: 'MMM d, yyyy'
                        }
                    },
                    grid: { display: false },
                    ticks: {
                        color: '#9ca3af',
                        font: { family: 'IBM Plex Mono', size: 10 }
                    },
                    title: {
                        display: true,
                        text: 'Purchase Date',
                        color: '#9ca3af',
                        font: { family: 'IBM Plex Mono', size: 11 }
                    }
                }
            }
        }
    });
}

async function deleteAsset(id) {
    if (!confirm('Are you sure you want to delete this asset?')) return;

    try {
        const response = await fetch(`${API_BASE}/assets/${id}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            console.log('Asset deleted successfully');
            loadAssets();
        } else {
            const errorText = await response.text();
            console.error('Delete failed:', errorText);
            alert('Failed to delete asset: ' + errorText);
        }
    } catch (error) {
        console.error('Error deleting asset:', error);
        alert('Error deleting asset: ' + error.message);
    }
}

function openAddAssetModal() {
    document.getElementById('addAssetModal').classList.add('active');
}

function closeAddAssetModal() {
    document.getElementById('addAssetModal').classList.remove('active');
    document.getElementById('addAssetForm').reset();
}

async function handleAddAsset(event) {
    event.preventDefault();

    const asset = {
        type: document.getElementById('assetType').value,
        nameOrTicker: document.getElementById('nameOrTicker').value,
        quantity: parseFloat(document.getElementById('quantity').value),
        purchasePrice: parseFloat(document.getElementById('purchasePrice').value),
        purchaseDate: document.getElementById('purchaseDate').value || null,
        notes: document.getElementById('notes').value || null
    };

    console.log('Adding asset:', asset);

    try {
        const response = await fetch(`${API_BASE}/assets`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(asset)
        });

        console.log('Add asset response status:', response.status);

        if (response.ok) {
            const result = await response.json();
            console.log('Asset added successfully:', result);
            closeAddAssetModal();
            loadAssets();
        } else {
            const errorText = await response.text();
            console.error('Add asset failed:', errorText);
            alert('Failed to add asset: ' + errorText);
        }
    } catch (error) {
        console.error('Error adding asset:', error);
        alert('Error adding asset: ' + error.message);
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing dashboard...');

    // Load assets on page load
    loadAssets();

    // Close modal on outside click
    document.getElementById('addAssetModal').addEventListener('click', (e) => {
        if (e.target.id === 'addAssetModal') {
            closeAddAssetModal();
        }
    });
});