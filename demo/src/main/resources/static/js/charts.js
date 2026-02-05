/**
 * Portfolio Manager - Charts Module
 * Uses ApexCharts for portfolio visualization
 */

// Chart instances
let allocationChart = null;
let performanceChart = null;

// Color palette
const chartColors = {
    STOCK: '#818cf8',
    BOND: '#22d3ee',
    ETF: '#fbbf24',
    MUTUAL_FUND: '#a78bfa',
    CRYPTO: '#f97316',
    REAL_ESTATE: '#14b8a6',
    CASH: '#10b981'
};

const gradientColors = [
    ['#818cf8', '#6366f1'],
    ['#22d3ee', '#06b6d4'],
    ['#fbbf24', '#f59e0b'],
    ['#a78bfa', '#8b5cf6'],
    ['#f97316', '#ea580c'],
    ['#14b8a6', '#0d9488'],
    ['#10b981', '#059669']
];

// ===================================
// Theme-Aware Colors
// ===================================
function getThemeColors() {
    const isDark = document.documentElement.getAttribute('data-theme') !== 'light';
    return {
        text: isDark ? '#f1f5f9' : '#1e293b',
        textMuted: isDark ? '#94a3b8' : '#64748b',
        background: isDark ? '#1e293b' : '#ffffff',
        border: isDark ? '#334155' : '#e2e8f0',
        mode: isDark ? 'dark' : 'light'
    };
}

// ===================================
// Chart Configuration
// ===================================
function getBaseChartOptions() {
    const colors = getThemeColors();
    return {
        chart: {
            background: 'transparent',
            foreColor: colors.textMuted,
            fontFamily: 'Inter, sans-serif',
            toolbar: {
                show: false
            },
            animations: {
                enabled: true,
                easing: 'easeinout',
                speed: 800
            }
        },
        theme: {
            mode: colors.mode
        },
        tooltip: {
            theme: colors.mode,
            style: {
                fontSize: '12px'
            }
        }
    };
}

// ===================================
// Allocation Pie Chart
// ===================================
function createAllocationChart(allocation) {
    const container = document.getElementById('allocation-chart');
    const themeColors = getThemeColors();
    const baseChartOptions = getBaseChartOptions();

    if (!allocation || Object.keys(allocation).length === 0) {
        container.innerHTML = `<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: ${themeColors.textMuted};">No allocation data available</div>`;
        return;
    }

    const labels = Object.keys(allocation);
    const series = Object.values(allocation).map(v => parseFloat(v) || 0);
    const colors = labels.map(type => chartColors[type] || '#64748b');

    const options = {
        ...baseChartOptions,
        series: series,
        labels: labels.map(formatAssetTypeLabel),
        chart: {
            ...baseChartOptions.chart,
            type: 'donut',
            height: 320
        },
        colors: colors,
        plotOptions: {
            pie: {
                donut: {
                    size: '65%',
                    labels: {
                        show: true,
                        name: {
                            show: true,
                            fontSize: '14px',
                            color: themeColors.text
                        },
                        value: {
                            show: true,
                            fontSize: '20px',
                            fontWeight: 600,
                            color: themeColors.text,
                            formatter: function (val) {
                                return parseFloat(val).toFixed(1) + '%';
                            }
                        },
                        total: {
                            show: true,
                            label: 'Total',
                            color: themeColors.textMuted,
                            formatter: function () {
                                return '100%';
                            }
                        }
                    }
                }
            }
        },
        legend: {
            show: true,
            position: 'bottom',
            horizontalAlign: 'center',
            labels: {
                colors: themeColors.text
            },
            markers: {
                width: 12,
                height: 12,
                radius: 3
            }
        },
        dataLabels: {
            enabled: false
        },
        stroke: {
            show: true,
            width: 2,
            colors: [themeColors.background]
        },
        responsive: [{
            breakpoint: 480,
            options: {
                chart: {
                    height: 280
                },
                legend: {
                    position: 'bottom'
                }
            }
        }]
    };

    if (allocationChart) {
        allocationChart.destroy();
    }

    container.innerHTML = '';
    allocationChart = new ApexCharts(container, options);
    allocationChart.render();
}

// ===================================
// Performance Bar Chart
// ===================================
function createPerformanceChart(assets) {
    const container = document.getElementById('performance-chart');
    const themeColors = getThemeColors();
    const baseChartOptions = getBaseChartOptions();

    if (!assets || assets.length === 0) {
        container.innerHTML = `<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: ${themeColors.textMuted};">No performance data available</div>`;
        return;
    }

    // Group assets by type and calculate totals
    const typePerformance = {};
    assets.forEach(asset => {
        if (!typePerformance[asset.type]) {
            typePerformance[asset.type] = {
                value: 0,
                cost: 0,
                gainLoss: 0
            };
        }
        typePerformance[asset.type].value += parseFloat(asset.currentValue) || 0;
        typePerformance[asset.type].cost += parseFloat(asset.costBasis) || 0;
        typePerformance[asset.type].gainLoss += parseFloat(asset.gainLoss) || 0;
    });

    const types = Object.keys(typePerformance);
    const values = types.map(t => typePerformance[t].value);
    const gainLoss = types.map(t => typePerformance[t].gainLoss);
    const colors = types.map(type => chartColors[type] || '#64748b');

    const options = {
        ...baseChartOptions,
        series: [
            {
                name: 'Current Value',
                data: values.map(v => parseFloat(v.toFixed(2)))
            },
            {
                name: 'Gain/Loss',
                data: gainLoss.map(v => parseFloat(v.toFixed(2)))
            }
        ],
        chart: {
            ...baseChartOptions.chart,
            type: 'bar',
            height: 320,
            stacked: false
        },
        colors: ['#6366f1', '#10b981'],
        plotOptions: {
            bar: {
                horizontal: false,
                columnWidth: '55%',
                borderRadius: 4,
                dataLabels: {
                    position: 'top'
                }
            }
        },
        dataLabels: {
            enabled: false
        },
        xaxis: {
            categories: types.map(formatAssetTypeLabel),
            labels: {
                style: {
                    colors: themeColors.textMuted,
                    fontSize: '11px'
                }
            },
            axisBorder: {
                show: false
            },
            axisTicks: {
                show: false
            }
        },
        yaxis: {
            labels: {
                style: {
                    colors: themeColors.textMuted
                },
                formatter: function (val) {
                    return formatCompactCurrency(val);
                }
            }
        },
        grid: {
            borderColor: themeColors.border,
            strokeDashArray: 4,
            xaxis: {
                lines: {
                    show: false
                }
            }
        },
        legend: {
            show: true,
            position: 'top',
            horizontalAlign: 'right',
            labels: {
                colors: themeColors.text
            }
        },
        tooltip: {
            ...baseChartOptions.tooltip,
            y: {
                formatter: function (val) {
                    return '$' + val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
                }
            }
        },
        responsive: [{
            breakpoint: 480,
            options: {
                chart: {
                    height: 280
                },
                plotOptions: {
                    bar: {
                        columnWidth: '70%'
                    }
                }
            }
        }]
    };

    if (performanceChart) {
        performanceChart.destroy();
    }

    container.innerHTML = '';
    performanceChart = new ApexCharts(container, options);
    performanceChart.render();
}

// ===================================
// Utility Functions
// ===================================
function formatAssetTypeLabel(type) {
    const typeMap = {
        'STOCK': 'Stocks',
        'BOND': 'Bonds',
        'ETF': 'ETFs',
        'MUTUAL_FUND': 'Mutual Funds',
        'CRYPTO': 'Crypto',
        'REAL_ESTATE': 'Real Estate',
        'CASH': 'Cash'
    };
    return typeMap[type] || type;
}

function formatCompactCurrency(value) {
    if (value >= 1000000) {
        return '$' + (value / 1000000).toFixed(1) + 'M';
    } else if (value >= 1000) {
        return '$' + (value / 1000).toFixed(1) + 'K';
    } else if (value <= -1000000) {
        return '-$' + (Math.abs(value) / 1000000).toFixed(1) + 'M';
    } else if (value <= -1000) {
        return '-$' + (Math.abs(value) / 1000).toFixed(1) + 'K';
    }
    return '$' + value.toFixed(0);
}

// ===================================
// Main Update Function
// ===================================
function updateCharts(summary) {
    // Create allocation chart
    if (summary.allocationByType) {
        createAllocationChart(summary.allocationByType);
    }

    // Create performance chart
    if (summary.assets) {
        createPerformanceChart(summary.assets);
    }
}

// Make updateCharts available globally
window.updateCharts = updateCharts;
