package com.vbs.demo.controller;

import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.TransactionStatus;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.UserRepo;
import com.vbs.demo.repositories.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    UserRepo userRepo;

    @PostMapping("/deposit")
    public String deposit(@RequestBody TransactionDto d)
    {
        User user = userRepo.findById(d.getId()).orElseThrow(()->new RuntimeException("User not found!"));
        double newBalance = user.getBalance() + d.getAmount();
        user.setBalance(newBalance);
        userRepo.save(user);

        Transaction t = new Transaction();
        t.setAmount(d.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("Rs. "+d.getAmount()+" Deposit Successful");
        t.setUserId(d.getId());
        t.setStatus(TransactionStatus.SUCCESS);
        transactionRepo.save(t);

        return "Deposit Successful!";
    }
    @PostMapping("/withdraw")
    public String withdraw(@RequestBody TransactionDto w)
    {
        User user = userRepo.findById(w.getId()).orElseThrow(()->new RuntimeException("User not found!"));
        Transaction t = new Transaction();
        t.setAmount(w.getAmount());
        t.setUserId(w.getId());

        if(w.getAmount() > user.getBalance())
        {
            t.setCurrBalance(user.getBalance());
            t.setDescription("Rs. "+w.getAmount()+" Withdrawal Failed: Insufficient Balance");
            t.setStatus(TransactionStatus.FAILED);
            t.setFailureReason("Insufficient balance");
            transactionRepo.save(t);
            return "Insufficient Balance!";
        }
        double newBalance = user.getBalance() - w.getAmount();
        user.setBalance(newBalance);
        userRepo.save(user);
        t.setCurrBalance(newBalance);
        t.setDescription("Rs. "+w.getAmount()+" Withdrawal Successful");
        t.setStatus(TransactionStatus.SUCCESS);
        t.setFailureReason(null);
        transactionRepo.save(t);

        return "Withdrawal successful!";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDto t)
    {
        User sender = userRepo.findById(t.getId()).orElseThrow(()->new RuntimeException("User not found!"));
        User receiver = userRepo.findByUsername(t.getUsername());

        Transaction failTx = new Transaction();
        failTx.setAmount(t.getAmount());
        failTx.setUserId(sender.getId());
        failTx.setCurrBalance(sender.getBalance());
        failTx.setStatus(TransactionStatus.FAILED);

        if(receiver ==null)
        {
            failTx.setDescription("Transfer failed: Receiver username not found");
            failTx.setFailureReason("Receiver username not found");
            transactionRepo.save(failTx);
            return "Username not found!";
        }
        if(sender.getId()==receiver.getId())
        {
            failTx.setDescription("Transfer failed: Sender and receiver accounts are the same");
            failTx.setFailureReason("Sender and receiver accounts must be different");
            transactionRepo.save(failTx);
            return "Sender and receiver accounts must be different!";
        }
        if(t.getAmount() <= 0)
        {
            failTx.setDescription("Transfer failed: Invalid Amount");
            failTx.setFailureReason("Invalid amount");
            transactionRepo.save(failTx);
            return "Invalid amount!";
        }
        if(t.getAmount() > sender.getBalance())
        {
            failTx.setDescription("Transfer failed: Insufficient Balance");
            failTx.setFailureReason("Insufficient balance");
            transactionRepo.save(failTx);
            return "Insufficient Balance!";
        }
        double sBalance = sender.getBalance() - t.getAmount();
        sender.setBalance(sBalance);
        userRepo.save(sender);

        Transaction t1 = new Transaction();
        t1.setAmount(t.getAmount());
        t1.setCurrBalance(sBalance);
        t1.setDescription("Rs. "+t.getAmount()+" sent to "+receiver.getUsername());
        t1.setUserId(t.getId());
        t1.setStatus(TransactionStatus.SUCCESS);
        t1.setFailureReason(null);
        transactionRepo.save(t1);

        double rBalance = receiver.getBalance() + t.getAmount();
        receiver.setBalance(rBalance);
        userRepo.save(receiver);

        Transaction t2 = new Transaction();
        t2.setAmount(t.getAmount());
        t2.setCurrBalance(rBalance);
        t2.setDescription("Rs. "+t.getAmount()+" received from "+sender.getUsername());
        t2.setUserId(receiver.getId());
        t2.setStatus(TransactionStatus.SUCCESS);
        t2.setFailureReason(null);
        transactionRepo.save(t2);

        return "Transferred successfully!";
    }

    @GetMapping("/passbook/{id}")
    public List<Transaction> passbook(@PathVariable int id)
    {
        return transactionRepo.findAllByUserId(id);
    }
}
