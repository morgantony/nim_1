package com.netease.nim.rtskit.doodle;

import java.util.List;

/**
 */
public interface TransactionObserver {
    void onTransaction(List<Transaction> transactions);
}
