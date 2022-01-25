import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CountDownLatch;

public class butikkTraader { //En simulering av en butikk ved hjelp av traader.
    public static void main(String[] args) throws InterruptedException {
        butikkTraader butikk = new butikkTraader();
        int antKunder = 40; //alternativt: int kunder = Integer.parseInt(args[0]);
        CountDownLatch latch = new CountDownLatch(antKunder);

        butikk.aapneButikk(antKunder, latch);

        latch.await();
        System.out.println("Alle kunder har besøkt butikken \n");
    }

    private void aapneButikk(int antKunder, CountDownLatch latch) throws InterruptedException{
        monitorButikkKunder butikk = new monitorButikkKunder(10);

        for (int i = 0; i < antKunder; i++){
            Thread kunde = new Thread(new Kunde(butikk, latch), "kunde" + (i+1));
            kunde.start();
            Thread.sleep(1000); //En og en om gangen inn i butikken. 
        }
    }



    private class Kunde implements Runnable {

        private monitorButikkKunder butikk;
        private CountDownLatch latch;

        public Kunde(monitorButikkKunder butikk, CountDownLatch latch){
            this.butikk = butikk;
            this.latch = latch;
        }

        @Override
        public void run() {
           try {
           
            butikk.enterStore();
            Thread.sleep(15000); //En kunde bruker 15 sekunder i butikken
            butikk.leaveStore();
            latch.countDown(); //Signaliserer at en kunde er ferdig i butikken.

        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
        }

    }


    private class monitorButikkKunder {

        private Lock doors;
        private Condition ikkeFullButikk;
        private int antallIButikken = 0;
        private final int BUTIKK_KAPASITET;

        public monitorButikkKunder(int kapasitet){
            BUTIKK_KAPASITET = kapasitet;
            doors = new ReentrantLock();
            ikkeFullButikk = doors.newCondition();
        }

        private void enterStore() throws InterruptedException {
            doors.lock(); //Bare en om gangen kan gå inn og ut av butikken
            try {
                while (antallIButikken >= BUTIKK_KAPASITET) {
                    System.out.println(Thread.currentThread().getName() + " venter på å komme inn i butikken..");
                    System.out.println("\n");
                    ikkeFullButikk.await(); //Venter på at butikken ikke er full lenger
                }
                antallIButikken++;
                System.out.println(Thread.currentThread().getName() + " kom inn i butikken");
                System.out.println("Det er " + antallIButikken + " kunder i butikken.");
                System.out.println("\n");
                
            }
            finally {
                doors.unlock(); //Åpner for ny kunde som skal inn
            }
        }

        private void leaveStore() throws InterruptedException {
            doors.lock(); //Bare en om gangen kan gå inn og ut av butikken
            try {
                antallIButikken--;
                System.out.println(Thread.currentThread().getName() + " forlot butikken");
                System.out.println("Det er " + antallIButikken + " kunder i butikken.");
                System.out.println("\n");
                ikkeFullButikk.signal(); //Gir beskjed om at ny kunde kan komme inn
            }
            finally {
                doors.unlock(); //Åpner for ny kunde som skal inn
            }
        }
     
    }
}

