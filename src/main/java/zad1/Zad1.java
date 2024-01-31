package zad1;

import java.util.Random;

class ThreadExample {
    private static int globalVariable = 0;
    static class MyThread extends Thread
    {
        @Override
        public void run()
        {
            Random random = new Random();
            synchronized (this) {
                if (random.nextBoolean()) {
                    globalVariable++;
                } else {
                    globalVariable--;
                }
            }

        }
    }

    public static void main(String[] args) throws InterruptedException
    {
        int numThreads = 5;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++)
        {
            threads[i] = new MyThread();
            threads[i].start();
        }

        for (Thread thread : threads)
        {
            thread.join();
        }

        System.out.println("Final value of global variable: " + globalVariable);
    }
}
