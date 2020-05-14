package models;

import utils.Utility;

public class DashboardData {
    private double todaySales, totalSales, customerBalance, vendorBalance;
    private ChartData[] chartData;

    public double getTodaySales() {
        return todaySales;
    }
    public String getTodaySalesString() {
        return Utility.formatNumber(todaySales);
    }

    public void setTodaySales(double todaySales) {
        this.todaySales = todaySales;
    }

    public double getTotalSales() {
        return totalSales;
    }
    public String getTotalSalesString() {
        return Utility.formatNumber(totalSales);
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }

    public ChartData[] getChartData() {
        return chartData;
    }

    public void setChartData(ChartData[] chartData) {
        this.chartData = chartData;
    }
    public String getCustomerBalanceString(){
        return Utility.formatNumber(customerBalance);
    }

    public String getVendorBalanceString(){
        return Utility.formatNumber(vendorBalance);
    }


    public static class ChartData{
        String date;
        double sales;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public double getSales() {
            return sales;
        }

        public void setSales(double sales) {
            this.sales = sales;
        }
    }
}
