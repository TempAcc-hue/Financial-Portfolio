import { createChart, CrosshairMode } from 'https://unpkg.com/lightweight-charts@4.1.3/dist/lightweight-charts.standalone.production.mjs';

// Configuration
const API_BASE_URL = 'http://localhost:8080'; // Change this to your API URL
let chart = null;
let candlestickSeries = null;

// DOM Elements
const tickerInput = document.getElementById('tickerInput');
const searchBtn = document.getElementById('searchBtn');
const searchBtnText = document.getElementById('searchBtnText');
const searchBtnLoader = document.getElementById('searchBtnLoader');
const resultsContainer = document.getElementById('resultsContainer');
const loadingContainer = document.getElementById('loadingContainer');
const errorContainer = document.getElementById('errorContainer');
const errorMessage = document.getElementById('errorMessage');

// Event Listeners
searchBtn.addEventListener('click', handleSearch);
tickerInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        handleSearch();
    }
});

// Main search handler
async function handleSearch() {
    const ticker = tickerInput.value.trim().toUpperCase();

    if (!ticker) {
        showError('Please enter a stock ticker symbol');
        return;
    }

    // Reset UI
    hideError();
    hideResults();
    showLoading();
    setButtonLoading(true);

    try {
        // Fetch all data in parallel
        const [historyData, predictionData, newsData] = await Promise.all([
            fetchStockHistory(ticker),
            fetchPrediction(ticker),
            fetchNews(ticker)
        ]);

        // Update UI with data
        updateStockInfo(ticker, historyData);
        updatePrediction(predictionData, historyData);
        updateStatistics(historyData);
        updateChart(historyData);
        updateNews(newsData);

        // Show results
        hideLoading();
        showResults();

    } catch (error) {
        console.error('Error fetching data:', error);
        hideLoading();
        showError(error.message || 'Failed to fetch stock data. Please verify the ticker symbol and try again.');
    } finally {
        setButtonLoading(false);
    }
}

// API Calls
async function fetchStockHistory(ticker) {
    const response = await fetch(`${API_BASE_URL}/api/v1/stocks/${ticker}/history`);

    if (!response.ok) {
        throw new Error(`Stock not found: ${ticker}`);
    }

    const data = await response.json();

    if (!data || data.length === 0) {
        throw new Error('No historical data available for this ticker');
    }

    return data;
}

async function fetchPrediction(ticker) {
    const response = await fetch(`${API_BASE_URL}/api/v1/stocks/${ticker}/predict`);

    if (!response.ok) {
        throw new Error('ML prediction not available');
    }

    return await response.json();
}

async function fetchNews(ticker) {
    const response = await fetch(`${API_BASE_URL}/api/news/company/${ticker}`);

    if (!response.ok) {
        return { success: false, data: [] };
    }

    return await response.json();
}

// Update UI Functions
function updateStockInfo(ticker, historyData) {
    const latestData = historyData[historyData.length - 1];
    const previousData = historyData[historyData.length - 2];

    document.getElementById('stockTicker').textContent = ticker;
    document.getElementById('currentPrice').textContent = `$${latestData.close.toFixed(2)}`;

    const priceChange = ((latestData.close - previousData.close) / previousData.close) * 100;
    const priceChangeElement = document.getElementById('priceChange');
    priceChangeElement.textContent = `${priceChange >= 0 ? '+' : ''}${priceChange.toFixed(2)}%`;
    priceChangeElement.className = priceChange >= 0 ? 'price-change positive' : 'price-change negative';
}

function updatePrediction(predictionData, historyData) {
    const latestPrice = historyData[historyData.length - 1].close;
    const predictedPrice = predictionData.predicted_price;

    document.getElementById('predictedPrice').textContent = `$${predictedPrice.toFixed(2)}`;

    const difference = ((predictedPrice - latestPrice) / latestPrice) * 100;
    const indicator = document.getElementById('predictionIndicator');
    const sentimentText = document.getElementById('sentimentText');

    if (difference > 2) {
        indicator.className = 'indicator bullish';
        sentimentText.textContent = 'Bullish';
    } else if (difference < -2) {
        indicator.className = 'indicator bearish';
        sentimentText.textContent = 'Bearish';
    } else {
        indicator.className = 'indicator neutral';
        sentimentText.textContent = 'Neutral';
    }
}

function updateStatistics(historyData) {
    const latestData = historyData[historyData.length - 1];

    // Calculate 24h high and low
    const last24h = historyData.slice(-1);
    const high24h = Math.max(...last24h.map(d => d.high));
    const low24h = Math.min(...last24h.map(d => d.low));

    // Calculate volatility (simplified)
    const returns = [];
    for (let i = 1; i < historyData.length; i++) {
        returns.push((historyData[i].close - historyData[i-1].close) / historyData[i-1].close);
    }
    const volatility = Math.sqrt(returns.reduce((sum, r) => sum + r * r, 0) / returns.length) * 100;

    document.getElementById('volume24h').textContent = formatVolume(latestData.volume);
    document.getElementById('high24h').textContent = `$${high24h.toFixed(2)}`;
    document.getElementById('low24h').textContent = `$${low24h.toFixed(2)}`;
    document.getElementById('volatility').textContent = `${volatility.toFixed(2)}%`;
}

function updateChart(historyData) {
    // Destroy existing chart if any
    if (chart) {
        chart.remove();
        chart = null;
    }

    const chartContainer = document.getElementById('chart');

    // Create new chart with dark theme
    chart = createChart(chartContainer, {
        width: chartContainer.clientWidth,
        height: 500,
        layout: {
            background: { color: 'transparent' },
            textColor: '#9ca3af',
        },
        grid: {
            vertLines: { color: '#2a2f3f' },
            horzLines: { color: '#2a2f3f' },
        },
        crosshair: {
            mode: CrosshairMode.Normal,
            vertLine: {
                color: '#d4af37',
                width: 1,
                style: 1,
            },
            horzLine: {
                color: '#d4af37',
                width: 1,
                style: 1,
            },
        },
        rightPriceScale: {
            borderColor: '#2a2f3f',
        },
        timeScale: {
            borderColor: '#2a2f3f',
            timeVisible: true,
            secondsVisible: false,
        },
    });

    // Create candlestick series with custom colors
    candlestickSeries = chart.addCandlestickSeries({
        upColor: '#10b981',
        downColor: '#ef4444',
        borderVisible: false,
        wickUpColor: '#10b981',
        wickDownColor: '#ef4444',
    });

    // Set data
    candlestickSeries.setData(historyData);
    chart.timeScale().fitContent();

    // Handle window resize
    const resizeObserver = new ResizeObserver(entries => {
        if (chart) {
            chart.applyOptions({ width: chartContainer.clientWidth });
        }
    });
    resizeObserver.observe(chartContainer);
}

function updateNews(newsData) {
    const newsContainer = document.getElementById('newsContainer');
    newsContainer.innerHTML = '';

    if (!newsData.success || !newsData.data || newsData.data.length === 0) {
        newsContainer.innerHTML = '<p class="text-secondary" style="text-align: center; padding: 2rem;">No news available for this ticker</p>';
        return;
    }

    // Display up to 6 news articles
    const articles = newsData.data.slice(0, 6);

    articles.forEach(article => {
        const articleElement = createNewsArticle(article);
        newsContainer.appendChild(articleElement);
    });
}

function createNewsArticle(article) {
    const articleDiv = document.createElement('div');
    articleDiv.className = 'news-article';
    articleDiv.onclick = () => window.open(article.url, '_blank');

    const date = new Date(article.datetime * 1000);
    const formattedDate = date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
    });

    articleDiv.innerHTML = `
        <img src="${article.image}" alt="${article.headline}" class="news-image" onerror="this.src='https://via.placeholder.com/400x200/0a0e1a/9ca3af?text=No+Image'">
        <div class="news-content">
            <div class="news-source">
                <span class="source-name">${article.source}</span>
                <span class="news-date">${formattedDate}</span>
            </div>
            <h4 class="news-headline">${article.headline}</h4>
            <p class="news-summary">${article.summary}</p>
        </div>
    `;

    return articleDiv;
}

// Utility Functions
function formatVolume(volume) {
    if (volume >= 1000000000) {
        return (volume / 1000000000).toFixed(2) + 'B';
    } else if (volume >= 1000000) {
        return (volume / 1000000).toFixed(2) + 'M';
    } else if (volume >= 1000) {
        return (volume / 1000).toFixed(2) + 'K';
    }
    return volume.toString();
}

// UI State Management
function showLoading() {
    loadingContainer.classList.remove('hidden');
}

function hideLoading() {
    loadingContainer.classList.add('hidden');
}

function showResults() {
    resultsContainer.classList.remove('hidden');
}

function hideResults() {
    resultsContainer.classList.add('hidden');
}

function showError(message) {
    errorMessage.textContent = message;
    errorContainer.classList.remove('hidden');
}

function hideError() {
    errorContainer.classList.add('hidden');
}

function setButtonLoading(isLoading) {
    searchBtn.disabled = isLoading;
    if (isLoading) {
        searchBtnText.classList.add('hidden');
        searchBtnLoader.classList.remove('hidden');
    } else {
        searchBtnText.classList.remove('hidden');
        searchBtnLoader.classList.add('hidden');
    }
}