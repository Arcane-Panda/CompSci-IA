import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;


/**
 * Event handler for Discord bot
 */
public class Commands extends ListenerAdapter
{
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        //Stops the bot from responding to other bots
        if(event.getAuthor().isBot())
        {
            return;
        }
        //gets user input from discord
        String arguments = event.getMessage().getContentRaw();

        //if the user's input is equal to one of the commands, send the appropriate message
        if(arguments.equalsIgnoreCase("?help") || arguments.equalsIgnoreCase("?info"))
        {
 
            event.getChannel().sendTyping().complete();
            event.getChannel().sendMessage("Use ?events or ?schedule to see the dates for upcoming sessions").complete();
           

        }else if(arguments.equalsIgnoreCase("?events") || arguments.equalsIgnoreCase("?schedule") )
        {   
            event.getChannel().sendTyping().complete();
            event.getChannel().sendMessage("__**Planned Sessions**__").complete();
            event.getChannel().sendMessage("**yyyy/mm/dd | Cancelled | Campaign**").complete();
            for(Event calendarEvent : CalendarProject.plannedEvents)
            {   
                event.getChannel().sendTyping().complete();
                event.getChannel().sendMessage( 
                String.valueOf(calendarEvent.getYear()) + "/" + String.valueOf(calendarEvent.getMonth()) + "/" + String.valueOf(calendarEvent.getDay()) +
               " | " + String.valueOf(calendarEvent.getCancelled()) + " | " + String.valueOf(calendarEvent.getGroup())).complete();
            }
        } else
        {
           // event.getChannel().sendMessage(arguments).complete();
        }
    }
}