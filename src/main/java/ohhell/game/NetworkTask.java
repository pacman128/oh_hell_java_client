package ohhell.game;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Network task.
 * This class maintains a thread that all network I/O uses
 */
public class NetworkTask {
    /** Logger. */
    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getPackage().getName());

    /** Wait time for an I/O read event */
    private final long delay;

    /** Thread pool executor with a single thread */
    private final ExecutorService service = Executors.newFixedThreadPool(1);

    /** Active connection flag */
    private final AtomicBoolean active = new AtomicBoolean();

    /** Listener for end task event */
    private final Listener listener;

    /** Server message processor */
    private MessageProcessor msgProcessor;

    /** Interface for task ended event */
    public interface Listener {
        /** Notification that task ended */
        void taskEnded();
    }

    /**
     * Create a new NetworkTask
     * @param listener Listener for task ended event
     * @param delay Wait time for I/O read events
     */
    public NetworkTask( Listener listener, long delay) {
        this.delay = delay;
        this.listener = listener;
    }

    /**
     * Start the network task
     * @param msgProcessor Server message processor
     */
    public void start(final MessageProcessor msgProcessor) {
        this.msgProcessor = msgProcessor;
        active.set(true);
        service.submit(this::task);
    }

    /**
     * End network task
     *
     * @throws InterruptedException If interrupted
     */
    public void end() throws InterruptedException {
        active.set(false);
    }

    /**
     * Submit a task into the service queue to run
     * @param task Subtask to run
     */
    public void submit(Callable<Void> task) {
        service.submit(task);
    }

    /**
     * Normal task run in service.
     * This task sends data to the message processor and if task is still active
     * resubmits itself to the service queue.
     */
    private void task() {
       if ( active.get()) {
           try {
               if (!msgProcessor.process(delay)) {
                   logger.fine("Network task ending");
                   active.set(false);
                   listener.taskEnded();
                   return;
               }
           } catch (IOException e) {
               logger.severe("Error: " + e.getLocalizedMessage());
           }
           service.submit(this::task);
       }
    }
}
