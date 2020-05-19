import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import javax.security.auth.login.LoginException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class CalendarProject {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    static List<Event> plannedEvents = new ArrayList<Event>();

    /**
     * Global instance of the scopes required by this application
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = CalendarProject.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    } 


    public static void main(String... args) throws IOException, GeneralSecurityException, FileNotFoundException, LoginException, InterruptedException, URISyntaxException, ClassNotFoundException, InstantiationException, UnsupportedLookAndFeelException, IllegalAccessException {
       
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "YOUR_ID_HERE";
        final String range = "A2:E";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();

        //Get values from spreadsheet and assign them to event objects
        if (values == null || values.isEmpty()) {
            throw new FileNotFoundException("Unable to access spreadsheet/spreadsheet is empty");
        } else {
            System.out.println("YYYY/MM/DD, Status, Group");
            for (List<Object> row : values) {
                System.out.printf("%s/%s/%s, %s, %s\n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4));
                plannedEvents.add(new Event(Integer.parseInt((String)row.get(0)), Integer.parseInt((String)row.get(1)), 
                Integer.parseInt((String)row.get(2)), Integer.parseInt((String)row.get(3)) ,(String)row.get(4)));           
            }
        }

        //Discord
        //Set up new JDA instance and connect to server
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = "YOUR_BOT_TOKEN_HERE"; 
        builder.setToken(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setGame(Game.playing("scheduling | ?info"));
        builder.addEventListener(new Commands());
        JDA jda = builder.buildBlocking();    
        Guild guild = jda.getGuildById("492090103016062987");    
        TextChannel announcements = guild.getTextChannelById("584270461421092904");
      
        
        //Creates calendar GUI
         CalendarGUI calendar = new CalendarGUI(plannedEvents);
         try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
        catch (ClassNotFoundException e) {
            throw new ClassNotFoundException();
        }
        catch (InstantiationException e) {
            throw new InstantiationException();
        }
        catch (IllegalAccessException e) {
            throw new IllegalAccessException();
        }
        catch (UnsupportedLookAndFeelException e) {
            throw new UnsupportedLookAndFeelException("");
        }
         //Prepare frame
         calendar.frmMain = new JFrame ("Calendar App"); //Create frame
         calendar.frmMain.setSize(330, 375); //Set size to 400x400 pixels
         calendar.pane = calendar.frmMain.getContentPane(); //Get content pane
         calendar.pane.setLayout(null); //Apply null layout

         //On window close, ask user if they're sure
         calendar.frmMain.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(calendar.frmMain, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                        //Send notifications to the annoucements channel if there is an event planned/cancelled tomorrow
                        for(Event event : plannedEvents)
                        {
                            if(calendar.realDay + 1 == event.getDay() && calendar.realMonth == event.getMonth() - 1 && calendar.realYear == event.getYear())
                            {
                                if(event.getCancelled())
                                {
                                    switch(event.getGroup())
                                    {
                                        default:
                                       //Send Messages
                                       annoucements.sendMessage("YOUR_MESSAGE_HERE");
                                       break;
                                    }
                                } else {
                                    switch(event.getGroup())
                                    {
                                        default:
                                       //Send Messages
                                       annoucements.sendMessage("YOUR_MESSAGE_HERE");
                                       break;
                                    }
                                } 
                            } else
                            {
                                GregorianCalendar cal = new GregorianCalendar(calendar.realYear, calendar.realMonth, 1);
                                int nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
                                if(calendar.realDay == nod)
                                {
                                    if(event.getDay() == 1 && calendar.realMonth+1 == event.getMonth() - 1 && calendar.realYear == event.getYear())
                                    {
                                        if(event.getCancelled())
                                        {
                                            switch(event.getGroup())
                                            {
                                                default:
                                                //Send Messages
                                                annoucements.sendMessage("YOUR_MESSAGE_HERE");
                                                break;
                                            }
                                        } else {
                                            switch(event.getGroup())
                                            {
                                                default:
                                                //Send Messages
                                                annoucements.sendMessage("YOUR_MESSAGE_HERE");
                                                break;
                                            }
                                        } 
                                    }
                                }
                            }
                        }    
                    System.exit(0);
                } else
                {
                    calendar.frmMain.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                }
            }
        });
        
         
         //Create controls
         calendar.lblMonth = new JLabel ("January");
         calendar.lblYear = new JLabel ("Change year:");
         calendar.cmbYear = new JComboBox();
         calendar.btnPrev = new JButton ("<--");
         calendar.btnNext = new JButton ("-->");
         calendar.mtblCalendar = new DefaultTableModel(){public boolean isCellEditable(int rowIndex, int mColIndex){return false;}};
         calendar.tblCalendar = new JTable(calendar.mtblCalendar);
         calendar.stblCalendar = new JScrollPane(calendar.tblCalendar);
         calendar.pnlCalendar = new JPanel(null);
         
         //Set border
         calendar.pnlCalendar.setBorder(BorderFactory.createTitledBorder("Calendar"));
         
         //Register action listeners
         calendar.btnPrev.addActionListener(new CalendarGUI.btnPrev_Action());
         calendar.btnNext.addActionListener(new CalendarGUI.btnNext_Action());
         calendar.cmbYear.addActionListener(new CalendarGUI.cmbYear_Action());
         calendar.tblCalendar.getSelectionModel().addListSelectionListener(new CalendarGUI.SharedListSelectionHandler());
         
         //Add controls to pane
         calendar.pane.add(calendar.pnlCalendar);
         calendar.pnlCalendar.add(calendar.lblMonth);
         calendar.pnlCalendar.add(calendar.lblYear);
         calendar.pnlCalendar.add(calendar.cmbYear);
         calendar.pnlCalendar.add(calendar.btnPrev);
         calendar.pnlCalendar.add(calendar.btnNext);
         calendar.pnlCalendar.add(calendar.stblCalendar);
         
         //Set bounds
         calendar.pnlCalendar.setBounds(0, 0, 320, 335);
         calendar.lblMonth.setBounds(160-calendar.lblMonth.getPreferredSize().width/2, 25, 100, 25);
         calendar.lblYear.setBounds(10, 305, 80, 20);
         calendar.cmbYear.setBounds(230, 305, 80, 20);
         calendar.btnPrev.setBounds(10, 25, 50, 25);
         calendar.btnNext.setBounds(260, 25, 50, 25);
         calendar.stblCalendar.setBounds(10, 50, 300, 250);
         
         //Make frame visible
         calendar.frmMain.setResizable(false);
         calendar.frmMain.setVisible(true);
         
         //Get real month/year
         GregorianCalendar cal = new GregorianCalendar(); //Create calendar
         calendar.realDay = cal.get(GregorianCalendar.DAY_OF_MONTH); //Get day
         calendar.realMonth = cal.get(GregorianCalendar.MONTH); //Get month
         calendar.realYear = cal.get(GregorianCalendar.YEAR); //Get year
         calendar.currentMonth = calendar.realMonth; //Match month and year
         calendar.currentYear = calendar.realYear;
         
         //Add headers
         String[] headers = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}; //All headers
         for (int i=0; i<7; i++){
            calendar.mtblCalendar.addColumn(headers[i]);
         }
         
         calendar.tblCalendar.getParent().setBackground(calendar.tblCalendar.getBackground()); //Set background
         
         //No resize/reorder
         calendar.tblCalendar.getTableHeader().setResizingAllowed(false);
         calendar.tblCalendar.getTableHeader().setReorderingAllowed(false);
         
         //Single cell selection
         calendar.tblCalendar.setColumnSelectionAllowed(true);
         calendar.tblCalendar.setRowSelectionAllowed(true);
         calendar.tblCalendar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         
         //Set row/column count
         calendar.tblCalendar.setRowHeight(38);
         calendar.mtblCalendar.setColumnCount(7);
         calendar.mtblCalendar.setRowCount(6);
         
         //Populate table
         for (int i=calendar.realYear-100; i<=calendar.realYear+100; i++){
            calendar.cmbYear.addItem(String.valueOf(i));
         }
         
         //Refresh calendar   
         calendar.refreshCalendar(calendar.realMonth, calendar.realYear); //Refresh calendar      
    }
}