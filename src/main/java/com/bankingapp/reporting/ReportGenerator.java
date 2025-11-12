package com.bankingapp.reporting;

import com.bankingapp.dao.TransactionDAO;
import com.bankingapp.model.Transaction;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {
    private final TransactionDAO transactionDAO;

    public ReportGenerator(TransactionDAO transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    /**
     * Generate a simple CSV-like account statement for the given account id.
     */
    public String generateAccountStatement(int accountId) throws SQLException {
        List<Transaction> txs = transactionDAO.getTransactionsByAccountId(accountId);
        StringBuilder sb = new StringBuilder();
        sb.append("id,type,amount,timestamp\n");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    for (Transaction t : txs) {
        sb.append(t.getId()).append(',')
            .append(t.getType()).append(',')
            .append(t.getAmount().setScale(2, java.math.RoundingMode.HALF_UP)).append(',')
            .append(t.getTimestamp().format(fmt))
            .append('\n');
    }
        return sb.toString();
    }
}
