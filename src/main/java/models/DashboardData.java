package models;

import utils.Utility;

public class DashboardData {
    private double todaySales, totalSales, customerBalance, vendorBalance;
    private ChartData[] chartData;
    private ChartData[] pnls;

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

    public ChartData[] getPnls() {
        return pnls;
    }

    public void setPnls(ChartData[] pnls) {
        this.pnls = pnls;
    }

    public static class ChartData{
        String name;
        double value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }
}
