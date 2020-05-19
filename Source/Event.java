/**
 * Event object that stores date information,
 * event status (planned/cancelled), and group
 * information.
 */
public class Event
{
    private int year, month, day;
    private String group;
    private boolean cancelled;

    public Event(int year, int month, int day, int cancelled, String group)
    {
        this.year = year;
        this.month = month;
        this.day = day;
        this.group = group;
        if (cancelled == 0)
        {
            this.cancelled = false;
        } else{
            this.cancelled = true;
        }
        
    }

    /**
     * Sets the event status to cancelled
     */
    public void cancelEvent()
    {
        cancelled = true;
    }

    /**
     * Returns the day the event is planned to occur on 
     * @return The day of the month (1-31)
     */
    public int getDay()
    {
        return day;
    }

    /**
     * Whether the event is cancelled or planned
     * @return false if the event is planned, and true if it has been cancelled. 
     */
    public boolean getCancelled()
    {
        return cancelled;
    }

    /**
     * The group that the event is planned for
     * @return the group name
     */
    public String getGroup()
    {
        return group;
    }

    /**
     * Returns the month the event is planned to occur on 
     * @return The month of the year (1-12)
     */
    public int getMonth()
    {
        return month;
    }

    /**
     * Returns the year the event is planned to occur in 
     * @return The planned year
     */
    public int getYear()
    {
        return year;
    }

}