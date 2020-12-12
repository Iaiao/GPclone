package yt.codechunk.gp.jobs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SecondaryJobCommand implements TabExecutor {
    private final HashMap<String, Job> secondaryJobs = new HashMap<String, Job>() {{
        put("НЕТ", Job.UNEMPLOYED);
        put("ЗЕЛЬЕВАР", Job.BREWER);
        put("ИНЖЕНЕР", Job.ENGINEER);
        put("МАГ", Job.WIZARD);
        put("КАРТОГРАФ", Job.CARTOGRAPHER);
        put("ХОДИТЕЛЬ_ПО_НЕЗЕРУ", Job.NETHER);
        put("РЫБАК", Job.FISHERMAN);
        put("ЮРИСТ", Job.LAWYER);
        put("КАМЕНЩИК", Job.MASON);
    }};

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length != 1) {
            return false;
        }
        if (secondaryJobs.containsKey(args[0].toUpperCase())) {
            Job job = secondaryJobs.get(args[0].toUpperCase());
            Jobs jobs = Main.getInstance().getJob(sender.getName());
            Main.getInstance().jobs.put(sender.getName().toLowerCase(), new Jobs(jobs == null ? Job.UNEMPLOYED : jobs.primary, job));
            sender.sendMessage("А теперь работай!");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        return (
                args[0].length() > 0 ?
                        secondaryJobs
                                .keySet()
                                .stream()
                                .filter(job -> job.startsWith(args[0].toUpperCase())) :
                        secondaryJobs.keySet().stream()
        )
                .map(job -> job.substring(0, 1).toUpperCase() + job.substring(1).toLowerCase())
                .collect(Collectors.toList());
    }
}
