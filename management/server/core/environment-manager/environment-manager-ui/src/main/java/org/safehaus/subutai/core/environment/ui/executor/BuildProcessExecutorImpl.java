package org.safehaus.subutai.core.environment.ui.executor;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;


/**
 * Created by bahadyr on 9/23/14.
 */
public class BuildProcessExecutorImpl implements BuildProcessExecutor
{
    private Set<BuildProcessExecutionListener> listeners = new HashSet<>();

    private EnvironmentBuildProcess buildProcess;


    public BuildProcessExecutorImpl( final EnvironmentBuildProcess buildProcess )
    {
        this.buildProcess = buildProcess;
    }


    @Override
    public void addListener( final BuildProcessExecutionListener listener )
    {
        this.listeners.add( listener );
    }


    @Override
    public void execute( final ExecutorService executor, final BuildProcessCommandFactory commandFactory )
    {
        final CompletionService<BuildProcessExecutionEvent> completionService =
                new ExecutorCompletionService( executor );

        completionService.submit( new Callable()
        {
            public BuildProcessExecutionEvent call()
            {
                fireEvent( new BuildProcessExecutionEvent( buildProcess.getUuid().toString(), "desc", BuildProcessExecutionEventType.START ) );
                try
                {
                    BuildProcessCommand command = commandFactory.newCommand();
                    command.execute();
                    return ( new BuildProcessExecutionEvent( buildProcess.getUuid().toString(), "desc", BuildProcessExecutionEventType.SUCCESS ) );
                }
                catch ( BuildProcessExecutionException ce )
                {
                    return ( new BuildProcessExecutionEvent( buildProcess.getUuid().toString(), "desc", BuildProcessExecutionEventType.FAIL ) );
                }
            }
        } );


        ExecutorService waiter = Executors.newSingleThreadExecutor();
        waiter.execute( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Future<BuildProcessExecutionEvent> future = completionService.take();
                    BuildProcessExecutionEvent result = future.get();
                    fireEvent( result );
                }
                catch ( InterruptedException | ExecutionException e )
                {
                    fireEvent( new BuildProcessExecutionEvent( buildProcess.getUuid().toString(), "desc", BuildProcessExecutionEventType.FAIL ) );
                }
            }
        } );
        waiter.shutdown();
    }


    private void fireEvent( BuildProcessExecutionEvent event )
    {
        for ( BuildProcessExecutionListener listener : listeners )
        {
            if ( listener != null )
            {
                listener.onExecutionEvent( event );
            }
        }
    }
}
