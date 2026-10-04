package view.Admin;

import controller.AnalyticsController;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import view.Dialogue.Dialogue_Confirmation;

/**
 *
 * @author kreggy
 */
public class AnalyticsPanel extends javax.swing.JPanel {
    private AnalyticsController analyticsController = new AnalyticsController();
    private DashboardForm parent;
    
    public AnalyticsPanel(DashboardForm parent) {
        this.parent = parent;
        initComponents();
        panelLineChart.setLayout(new java.awt.BorderLayout());
        loadDailyParkinChart();
        loadVehiclePieChart();
        
        updateDailyParkLabel();
        updateDailyReservesLabel();
        updateMostVehicleTypeLabel();
        updateBusyDayLabel();
    }

    void updateDailyParkLabel() {
        int avgDaily = analyticsController.getAverageDailyPark();
        lblDailyPark.setText(String.valueOf(avgDaily));
    }

    void updateDailyReservesLabel() {
        int avgReserves = analyticsController.getAverageDailyReserves();
        lblDailyReserves.setText(String.valueOf(avgReserves));
    }

    void updateMostVehicleTypeLabel() {
        String vehicle = analyticsController.getMostVehicleType();
        if(vehicle.equals("pwd")){
            vehicle = vehicle.toUpperCase();
        }else{
            vehicle = analyticsController.capitalize(vehicle);
        }
        lblTypeOfVehicle.setText(vehicle);
    }

    void updateBusyDayLabel() {
        String day = analyticsController.getMostBusyDay();
        lblBusyDay.setText(day);
    }

    public void loadDailyParkinChart() {
        var datasetMap = analyticsController.getDailyParkChartData();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        datasetMap.forEach((day, total) -> dataset.addValue(total, "Vehicles", day));

        JFreeChart chart = ChartFactory.createLineChart(
                "Daily Park-In",
                "Day",
                "Vehicles",
                dataset
        );

        chart.setBorderVisible(false);
        chart.setBackgroundPaint(java.awt.Color.WHITE);
        chart.removeLegend();

        var plot = chart.getCategoryPlot();
        plot.setOutlineVisible(false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);

        panelLineChart.removeAll();
        panelLineChart.setLayout(new java.awt.BorderLayout());
        panelLineChart.add(chartPanel, java.awt.BorderLayout.CENTER);
        panelLineChart.revalidate();
        panelLineChart.repaint();
    }

    public void loadVehiclePieChart() {
        var datasetMap = analyticsController.getVehiclePieChartData();
        DefaultPieDataset dataset = new DefaultPieDataset();

        datasetMap.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
                null,
                dataset,
                false,
                false,
                false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setOutlineVisible(false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);

        panelPieChart.removeAll();
        panelPieChart.setLayout(new java.awt.BorderLayout());
        panelPieChart.add(chartPanel, java.awt.BorderLayout.CENTER);
        panelPieChart.revalidate();
        panelPieChart.repaint();
    }
    
    public void getVehiclePieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        try {
            String sql = "SELECT vehicle, COUNT(*) AS total "
                       + "FROM parking_logs "
                       + "WHERE action_type='time_in' "
                       + "GROUP BY vehicle";

            java.sql.Connection conn = model.DBConnection.connect();
            java.sql.PreparedStatement ps = conn.prepareStatement(sql);
            java.sql.ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                String vehicle = rs.getString("vehicle");
                int total = rs.getInt("total");

                dataset.setValue(vehicle, total);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createPieChart(
                null,
                dataset,
                false,
                false,
                false
        );

        // Optional: nicer colors
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setOutlineVisible(false);

        ChartPanel chartPanel = new ChartPanel(chart);

        // Make chart responsive inside panel
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);

        // Add to your JPanel
        panelPieChart.removeAll();
        panelPieChart.setLayout(new java.awt.BorderLayout());
        panelPieChart.add(chartPanel, java.awt.BorderLayout.CENTER);
        panelPieChart.revalidate();
        panelPieChart.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel9 = new javax.swing.JLabel();
        panelLineChart = new javax.swing.JPanel();
        panelPieChart = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblDailyPark = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lblDailyReserves = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        lblTypeOfVehicle = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lblBusyDay = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Javanese Text", 1, 22)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 53, 48));
        jLabel9.setText("Analytics");
        add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(99, 39, -1, -1));

        javax.swing.GroupLayout panelLineChartLayout = new javax.swing.GroupLayout(panelLineChart);
        panelLineChart.setLayout(panelLineChartLayout);
        panelLineChartLayout.setHorizontalGroup(
            panelLineChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 980, Short.MAX_VALUE)
        );
        panelLineChartLayout.setVerticalGroup(
            panelLineChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 340, Short.MAX_VALUE)
        );

        add(panelLineChart, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 70, 980, 340));

        javax.swing.GroupLayout panelPieChartLayout = new javax.swing.GroupLayout(panelPieChart);
        panelPieChart.setLayout(panelPieChartLayout);
        panelPieChartLayout.setHorizontalGroup(
            panelPieChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 330, Short.MAX_VALUE)
        );
        panelPieChartLayout.setVerticalGroup(
            panelPieChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 220, Short.MAX_VALUE)
        );

        add(panelPieChart, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 440, 330, 220));

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        jLabel1.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 51, 51));
        jLabel1.setText("Average Daily Park");

        jLabel5.setFont(new java.awt.Font("Century Schoolbook", 0, 36)); // NOI18N

        lblDailyPark.setFont(new java.awt.Font("Century Schoolbook", 0, 36)); // NOI18N
        lblDailyPark.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDailyPark.setText("50");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(141, 141, 141)
                        .addComponent(jLabel5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addComponent(lblDailyPark, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDailyPark)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 440, 260, 100));

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        jLabel2.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 51, 51));
        jLabel2.setText("Average Daily Reservation");

        lblDailyReserves.setFont(new java.awt.Font("Century Schoolbook", 0, 36)); // NOI18N
        lblDailyReserves.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDailyReserves.setText("18");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addContainerGap(17, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblDailyReserves, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(82, 82, 82))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDailyReserves)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 440, 260, 100));

        jPanel3.setBackground(new java.awt.Color(204, 204, 204));
        jPanel3.setForeground(new java.awt.Color(0, 51, 51));

        jLabel4.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 51, 51));
        jLabel4.setText("Most Type of Vehicle Parked ");

        lblTypeOfVehicle.setFont(new java.awt.Font("Century Schoolbook", 0, 36)); // NOI18N
        lblTypeOfVehicle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTypeOfVehicle.setText("Car");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(lblTypeOfVehicle, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTypeOfVehicle)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 550, 260, 110));

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));

        jLabel3.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 51, 51));
        jLabel3.setText("Most Busy Day of the Week");

        lblBusyDay.setFont(new java.awt.Font("Century Schoolbook", 0, 36)); // NOI18N
        lblBusyDay.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBusyDay.setText("Monday");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblBusyDay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblBusyDay)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 550, 260, 110));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lblBusyDay;
    private javax.swing.JLabel lblDailyPark;
    private javax.swing.JLabel lblDailyReserves;
    private javax.swing.JLabel lblTypeOfVehicle;
    private javax.swing.JPanel panelLineChart;
    private javax.swing.JPanel panelPieChart;
    // End of variables declaration//GEN-END:variables
}