package com.denesgarda.Cent;

import java.util.Random;

public class Request {
    public int ID;
    public int target;
    public int key;
    public int start;
    public long begin;
    public long end;

    public Request(int ID, int target, int key, int start) {
        this.ID = ID;
        this.target = target;
        this.key = key;
        this.start = start;
        begin = System.nanoTime();
    }

    public long end() {
        end = System.nanoTime();
        return end - begin;
    }

    public static Request generate(int ID) {
        Random random = new Random();
        int target = random.nextInt(100000);
        int key = random.nextInt(10000);
        int start = target * key;
        return new Request(ID, target, key, start);
    }
}
