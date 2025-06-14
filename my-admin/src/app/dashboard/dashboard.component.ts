import { Component, OnInit, HostListener } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import html2canvas from 'html2canvas'; 

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: false, 
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  dashboardData: any;
  
  // Current periods for each section
  statsPeriod: string = 'week';
  revenuePeriod: string = 'month';
  activityPeriod: string = 'day';
  productsPeriod: string = 'month';
  
  // Charts
  revenueChart: any;
  productsChart: any;
  
  // Colors for products chart
  productColors: string[] = [
    "#5A3726", // Brown
    "#E73B3B", // Red
    "#F0CD56", // Yellow
    "#4FC3C3", // Teal
    "#81B13D"  // Green
  ];
  
  constructor(private http: HttpClient) { }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    // Redraw charts when window is resized
    if (this.dashboardData) {
      this.initializeCharts();
    }
  }

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.http.get('/data.json').subscribe(
      (data: any) => {
        console.log('Fetched data:', data); // Log fetched data
        this.dashboardData = data.dashboard_data;
        // Assign colors to top products
        if (this.dashboardData.top_products) {
          this.dashboardData.top_products.forEach((product: any, index: number) => {
            product.color = this.productColors[index % this.productColors.length];
          });
        }
        console.log('Processed dashboard data:', this.dashboardData); // Log processed data
        this.initializeCharts();
      },
      error => {
        console.error('Error loading dashboard data:', error);
      }
    );
  }

  initializeCharts() {
    setTimeout(() => {
      this.createRevenueChart();
      this.createProductsChart();
    }, 100);
  }

  createRevenueChart() {
    if (this.revenueChart) {
      this.revenueChart.destroy();
    }

    const revenueCtx = document.getElementById('revenue-chart') as HTMLCanvasElement;
    if (!revenueCtx) return;

    // Get data based on the selected period
    let labels: string[] = [];
    let currentData: number[] = [];
    let previousData: number[] = [];

    if (this.revenuePeriod === 'week') {
      labels = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
      // We'll use the revenue_by_day data here
      const revenueData = this.dashboardData.revenue_by_day;
      console.log('Revenue data for week:', revenueData); // Log revenue data
      currentData = [2500, 3200, 2100, 4000, 3700, 3000, 3200];
      previousData = [2000, 2800, 1900, 3500, 3200, 2700, 2900];
    } else if (this.revenuePeriod === 'month') {
      labels = ['Week 1', 'Week 2', 'Week 3', 'Week 4'];
      currentData = [9500, 11000, 8000, 15000];
      previousData = [8500, 10000, 7000, 13000];
    } else if (this.revenuePeriod === 'year') {
      labels = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      currentData = [30000, 28000, 32000, 35000, 28000, 33000, 38000, 36000, 40000, 42000, 45000, 48000];
      previousData = [25000, 24000, 28000, 30000, 25000, 29000, 32000, 31000, 35000, 38000, 40000, 42000];
    }

    this.revenueChart = new Chart(revenueCtx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Previous period',
            data: previousData,
            backgroundColor: '#000',
            borderColor: '#000',
            borderWidth: 0,
            borderRadius: {
              bottomLeft: 10,
              bottomRight: 10,
              topLeft: 0,
              topRight: 0
            },
            barPercentage: 0.4,
            categoryPercentage: 0.7
          },
          {
            label: 'Current period',
            data: currentData,
            backgroundColor: '#4D7F58',
            borderColor: '#4D7F58',
            borderWidth: 0,
            borderRadius: {
              bottomLeft: 0,
              bottomRight: 0,
              topLeft: 10,
              topRight: 10
            },
            barPercentage: 0.4,
            categoryPercentage: 0.7
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            enabled: true,
            backgroundColor: 'white',
            titleColor: '#3F2307',
            bodyColor: '#3F2307',
            borderColor: '#E0D5C5',
            borderWidth: 1,
            displayColors: false,
            callbacks: {
              title: function(context) {
                return context[0].label;
              },
              label: function(context) {
                if (context.parsed.y !== null) {
                  let valueTooltip = '';
                  if (context.datasetIndex === 1) {
                    valueTooltip = 'Current period';
                  } else {
                    valueTooltip = 'Previous period';
                  }
                  valueTooltip += ': $' + context.parsed.y.toLocaleString();
                  return valueTooltip;
                }
                return '';
              }
            }
          }
        },
        scales: {
          x: {
            stacked: true,
            grid: {
              display: false
            },
            ticks: {
              color: '#8C7B6B'
            }
          },
          y: {
            stacked: true,
            beginAtZero: true,
            grid: {
              display: false,
            },
            ticks: {
              color: '#8C7B6B',
              callback: function(value) {
                if (value === 0) return '0';
                if (value === 10000) return '$10,000';
                if (value === 20000) return '$20,000';
                if (value === 30000) return '$30,000';
                if (value === 40000) return '$40,000';
                if (value === 50000) return '$50,000';
                return '';
              }
            }
          }
        }
      }
    });
  }

  createProductsChart() {
    if (this.productsChart) {
      this.productsChart.destroy();
    }

    const productsCtx = document.getElementById('top-products-chart') as HTMLCanvasElement;
    if (!productsCtx) return;

    // Ensure the canvas has correct dimensions before creating the chart
    if (window.innerWidth <= 992) {
      productsCtx.style.height = '220px';
      productsCtx.style.width = '100%';
    }

    // Get top products data based on selected period
    const topProducts = this.dashboardData.top_products;
    let productData: any[] = [];

    if (this.productsPeriod === 'week') {
      productData = topProducts.map((product: any) => {
        return {
          name: product.name,
          value: product.weekly_sales.quantity_sold,
          salesValue: product.weekly_sales.total_sales,
          color: product.color
        };
      });
    } else if (this.productsPeriod === 'month') {
      productData = topProducts.map((product: any) => {
        return {
          name: product.name,
          value: product.monthly_sales.quantity_sold,
          salesValue: product.monthly_sales.total_sales,
          color: product.color
        };
      });
    } else if (this.productsPeriod === 'year') {
      productData = topProducts.map((product: any) => {
        return {
          name: product.name,
          value: product.yearly_sales.quantity_sold,
          salesValue: product.yearly_sales.total_sales,
          color: product.color
        };
      });
    }

    // Calculate percentages for the pie chart
    const total = productData.reduce((sum, item) => sum + item.value, 0);
    productData = productData.map(item => {
      return {
        ...item,
        percentage: Math.round((item.value / total) * 100)
      };
    });

    // Create the chart
    this.productsChart = new Chart(productsCtx, {
      type: 'doughnut',
      data: {
        labels: productData.map(item => item.name),
        datasets: [{
          data: productData.map(item => item.percentage),
          backgroundColor: productData.map(item => item.color),
          borderWidth: 0,
          hoverOffset: 5
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '70%',
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            enabled: true,
            backgroundColor: 'white',
            titleColor: '#3F2307',
            bodyColor: '#3F2307',
            borderColor: '#E0D5C5',
            borderWidth: 1,
            displayColors: false,
            callbacks: {
              label: function(context) {
                const item = productData[context.dataIndex];
                return `${item.name}: ${item.percentage}% ($${item.salesValue.toLocaleString()})`;
              }
            }
          }
        }
      }
    });
  }

  // Methods to update period selections
  updateStatsPeriod(period: string) {
    this.statsPeriod = period;
  }

  updateRevenuePeriod(period: string) {
    this.revenuePeriod = period;
    this.createRevenueChart();
  }

  updateActivityPeriod(period: string) {
    this.activityPeriod = period;
  }

  updateProductsPeriod(period: string) {
    this.productsPeriod = period;
    this.createProductsChart();
  }

  // Format numbers with commas
  formatNumber(num: number): string {
    return num.toLocaleString();
  }

  formatCurrency(value: number) {
    return value ? value.toLocaleString() : '0';
  }

  // Get stats data based on selected period
  getStatValue(stat: string): any {
    if (this.dashboardData && this.dashboardData[stat] && this.dashboardData[stat][this.statsPeriod]) {
      return this.dashboardData[stat][this.statsPeriod];
    }
    return { current: 0, growth_percentage: 0, growth_amount: 0 };
  }

  getRecentActivity(): any[] {
    if (this.dashboardData && this.dashboardData.recent_activity) {
      return this.dashboardData.recent_activity;
    }
    return [];
  }

  // Get top products based on selected period
  getTopProducts(): any[] {
    if (!this.dashboardData || !this.dashboardData.top_products) return [];
    
    const products = this.dashboardData.top_products.map((product: any, index: number) => {
      let salesData;
      if (this.productsPeriod === 'week') {
        salesData = product.weekly_sales;
      } else if (this.productsPeriod === 'month') {
        salesData = product.monthly_sales;
      } else {
        salesData = product.yearly_sales;
      }
      
      return {
        ...product,
        salesData,
        color: this.productColors[index % this.productColors.length]
      };
    });
    
    return products;
  }

  // Get customer data
  getCustomerData(customerId: string) {
    if (this.dashboardData && this.dashboardData.customer_data) {
      const customer = this.dashboardData.customer_data.find((c: any) => c.customer_id === customerId);
      return customer || { name: 'Unknown', phone: '', email: '' };
    }
    return { name: 'Unknown', phone: '', email: '' };
  }


  getCustomerInitials(name: string): string {
    if (!name) return 'UN';
    const parts = name.split(' ');
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  }

  exportCharts(): void {
    const exportElem = document.querySelector('.main-content') as HTMLElement;
    if (!exportElem) {
        return;
    }
    html2canvas(exportElem).then(canvas => {
        const link = document.createElement('a');
        link.href = canvas.toDataURL('image/png');
        link.download = 'dashboard.png';
        link.click();
    }).catch(error => {
        console.error('Lỗi khi export dashboard:', error);
    });
  }
}