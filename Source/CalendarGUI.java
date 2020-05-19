import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;

import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingConstants;
import java.awt.GridBagLayout;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Object in charge of the GUI of the application
 */
public class CalendarGUI {
    static JLabel lblMonth, lblYear;
    static JButton btnPrev, btnNext;
    static JTable tblCalendar;
    static JComboBox cmbYear;
    static JFrame frmMain;
    static Container pane;
    static DefaultTableModel mtblCalendar; // Table model
    static JScrollPane stblCalendar; // The scrollpane
    static JPanel pnlCalendar;
    static int realYear, realMonth, realDay, currentYear, currentMonth;

    static List<Event> plannedEvents;

    public CalendarGUI(List<Event> plannedEvents) {
        this.plannedEvents = plannedEvents;
    }

    /**
     * Draws the calendar to the frame
     * 
     * @param month the month to be displayed
     * @param year  the year to be displayed
     */
    public static void refreshCalendar(int month, int year) {
        // Variables
        String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December" };
        int nod, som; // Number Of Days, Start Of Month

        // Allow/disallow buttons
        btnPrev.setEnabled(true);
        btnNext.setEnabled(true);
        if (month == 0 && year <= realYear - 10) {
            btnPrev.setEnabled(false);
        } // Too early
        if (month == 11 && year >= realYear + 100) {
            btnNext.setEnabled(false);
        } // Too late
        lblMonth.setText(months[month]); // Refresh the month label (at the top)
        lblMonth.setBounds(160 - lblMonth.getPreferredSize().width / 2, 25, 180, 25); // Re-align label with calendar
        cmbYear.setSelectedItem(String.valueOf(year)); // Select the correct year in the combo box

        // Clear table
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                mtblCalendar.setValueAt(null, i, j);
            }
        }

        // Get first day of month and number of days
        GregorianCalendar cal = new GregorianCalendar(year, month, 1);
        nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        som = cal.get(GregorianCalendar.DAY_OF_WEEK);

        // Draw calendar
        for (int i = 1; i <= nod; i++) {
            int row = (i + som - 2) / 7;// new Integer((i+som-2)/7);
            int column = (i + som - 2) % 7;
            mtblCalendar.setValueAt(i, row, column);
        }

        // Apply renderers
        tblCalendar.setDefaultRenderer(tblCalendar.getColumnClass(0), new tblCalendarRenderer());
    }

    /**
     * Draws background colors for each cell of the calendar
     */
    static class tblCalendarRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            if (column == 0 || column == 6) { // Week-end
                setBackground(new Color(255, 220, 220));
                setBackground(new Color(224, 224, 224));
            } else { // Week
                setBackground(new Color(255, 255, 255));
               // setBackground(new Color(ThreadLocalRandom.current().nextInt(0, 255 + 1),ThreadLocalRandom.current().nextInt(0, 255 + 1),ThreadLocalRandom.current().nextInt(0, 255 + 1)));
            }
            if (value != null) {
                if (Integer.parseInt(value.toString()) == realDay && currentMonth == realMonth
                        && currentYear == realYear) { // Today
                    setBackground(new Color(220, 220, 255));
                        
                }

                for (Event event : plannedEvents) {
                    if (Integer.parseInt(value.toString()) == event.getDay() && currentMonth == event.getMonth() - 1
                            && currentYear == event.getYear()) { //Day of session
                        if (event.getCancelled()) {
                            setBackground(new Color(255, 220, 220));
                        } else {
                            setBackground(new Color(220, 255, 220));
                        }
                    }
                }
            }
            setBorder(null);
            setForeground(Color.black);
            return this;
        }
    }

    /**
     * Moves the month back one month when the "previous" button is clicked
     */
    static class btnPrev_Action implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (currentMonth == 0) { // Back one year
                currentMonth = 11;
                currentYear -= 1;
            } else { // Back one month
                currentMonth -= 1;
            }
            refreshCalendar(currentMonth, currentYear);
        }
    }

    /**
     * Moves the month forward one month when the "next" button is clicked
     */
    static class btnNext_Action implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (currentMonth == 11) { // Foward one year
                currentMonth = 0;
                currentYear += 1;
            } else { // Foward one month
                currentMonth += 1;
            }
            refreshCalendar(currentMonth, currentYear);
        }
    }

    /**
     * Changes the year to be displayed to the one selected by the user
     */
    static class cmbYear_Action implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (cmbYear.getSelectedItem() != null) {
                String b = cmbYear.getSelectedItem().toString();
                currentYear = Integer.parseInt(b);
                refreshCalendar(currentMonth, currentYear);
            }
        }
    }

    /**
     * When a row of the calendar is clicked, creates a new window containing all
     * the events that occur that week
     */
    static class SharedListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
                System.out.println("<none>");
            } else if (!e.getValueIsAdjusting()) {
                URI uri;
                try {
                    uri = new URI(
                            "https://docs.google.com/spreadsheets/d/1PGZGN5IwYh2MNbfn3TwrPHXcbxTQ9U20r-eN8Qxz8no/edit#gid=0");
                            class OpenUrlAction implements ActionListener
                            {
                                @Override public void actionPerformed(ActionEvent e)
                                {
                                    open(uri);
                                }
                            }
                            JButton button = new JButton();
                            button.setText("<HTML>Click the <FONT color=\"#000099\"><U>link</U></FONT>"
                                + " to add session dates.</HTML>");
                                button.setHorizontalAlignment(SwingConstants.LEFT);
                                button.setVerticalAlignment(SwingConstants.TOP);
                            button.setBorderPainted(false);
                            button.setOpaque(false);
                            button.setBackground(Color.WHITE);
                            button.setToolTipText(uri.toString());
                            button.addActionListener(new OpenUrlAction());
                            JFrame weekEvents = new JFrame("Selected Week's Events");
                            Container container = weekEvents.getContentPane();
                            container.setLayout(new GridBagLayout());
                            container.add(button);
                            weekEvents.setSize(500, 300);
                            weekEvents.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            weekEvents.setResizable(false);
                            weekEvents.setVisible(true);
                            JLabel dates = new JLabel("<html><b>Planned Sessions<br/>Week Day, yyyy/mm/dd | Cancelled | Campaign</b>");
                            // Find out which indexes are selected.
                            int minIndex = lsm.getMinSelectionIndex();
                            int maxIndex = lsm.getMaxSelectionIndex();
                            for (int i = minIndex; i <= maxIndex; i++) {
                                if (lsm.isSelectedIndex(i)) {
                                    for(int k = 0; k < 7; k++)
                                    {
                                        for(Event event : plannedEvents)
                                        {
                                            if(mtblCalendar.getValueAt(i, k) != null)
                                            {
                                                if((Integer)mtblCalendar.getValueAt(i, k) == event.getDay() && currentMonth == event.getMonth() -1 && currentYear == event.getYear())
                                                {
                                                   switch(k)
                                                   {    
                                                        case 0:
                                                            dates.setText(dates.getText() + "<br/>" + "Sunday, " + String.valueOf(event.getYear()) + "/" + String.valueOf(event.getMonth()) + "/" + String.valueOf(event.getDay()) + " | " + String.valueOf(event.getCancelled()) + " | " + String.valueOf(event.getGroup()) );
                                                            break;
                                                        case 1:
                                                            dates.setText(dates.getText() + "<br/>" + "Monday, " + String.valueOf(event.getYear()) + "/" + String.valueOf(event.getMonth()) + "/" + String.valueOf(event.getDay()) + " | " + String.valueOf(event.getCancelled()) + " | " + String.valueOf(event.getGroup()) );
                                                            break;
                                                        case 2:
                                                            dates.setText(dates.getText() + "<br/>" + "Tuesday, " + String.valueOf(event.getYear()) + "/" + String.valueOf(event.getMonth()) + "/" + String.valueOf(event.getDay()) + " | " + String.valueOf(event.getCancelled()) + " | " + String.valueOf(event.getGroup()) );
                                                            break;
                                                        case 3:
                                                            dates.setText(dates.getText() + "<br/>" + "Wednesday, " + String.valueOf(event.getYear()) + "/" + String.valueOf(event.getMonth()) + "/" + String.valueOf(event.getDay()) + " | " + String.valueOf(event.getCancelled()) + " | " + String.valueOf(event.getGroup()) );
                                                            break;
                                                        case 4:
                                                            dates.setText(dates.getText() + "<br/>" + "Thursday, " + String.valueOf(event.getYear()) + "/" + String.valueOf(event.getMonth()) + "/" + String.valueOf(event.getDay()) + " | " + String.valueOf(event.getCancelled()) + " | " + String.valueOf(event.getGroup()) );
                                                            break;
                                                        case 5:
                                                            dates.setText(dates.getText() + "<br/>" + "Friday, " + String.valueOf(event.getYear()) + "/" + String.valueOf(event.getMonth()) + "/" + String.valueOf(event.getDay()) + " | " + String.valueOf(event.getCancelled()) + " | " + String.valueOf(event.getGroup()) );
                                                            break;
                                                        case 6:
                                                            dates.setText(dates.getText() + "<br/>" + "Saturday, " + String.valueOf(event.getYear()) + "/" + String.valueOf(event.getMonth()) + "/" + String.valueOf(event.getDay()) +" | " + String.valueOf(event.getCancelled()) + " | " + String.valueOf(event.getGroup()) );
                                                            break;

                                                   }
                                                    
                                                  //  dates.setText(dates.getText() + "<br/>" + String.valueOf(event.getYear()) + "/" + String.valueOf(event.getMonth()) + "/" + String.valueOf(event.getDay()) +
                                                   // " | " + String.valueOf(event.getCancelled()) + " | " + String.valueOf(event.getGroup()) );
                                                    System.out.println(String.valueOf(event.getYear()) + "/" + String.valueOf(event.getMonth()) + "/" + String.valueOf(event.getDay()) +
                                                    " | " + String.valueOf(event.getCancelled()) + " | " + String.valueOf(event.getGroup()));
                                                }
                                            }
                                            
                                        }
                                    }
                                    

                                }
                            }
                            System.out.println("");
                            dates.setText(dates.getText() + "<br/>" + "No other dates found" + "</html>");
                            weekEvents.add(dates);
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
                
               
                
            }
            
        }
    }

    private static void open(URI uri)
    {
        if (Desktop.isDesktopSupported())
        {
            try{
                Desktop.getDesktop().browse(uri);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
   
}