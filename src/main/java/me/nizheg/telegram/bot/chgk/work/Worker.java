package me.nizheg.telegram.bot.chgk.work;

public interface Worker {

    boolean canDo(WorkDescription workDescription);

    void doWork(WorkDescription workDescription) throws WorkException;

}
