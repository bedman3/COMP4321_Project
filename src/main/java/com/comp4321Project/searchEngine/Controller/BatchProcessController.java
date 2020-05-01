package com.comp4321Project.searchEngine.Controller;

import com.comp4321Project.searchEngine.Dao.RocksDBDao;
import com.comp4321Project.searchEngine.Service.BatchProcessing;
import com.comp4321Project.searchEngine.View.Message;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class BatchProcessController {

    @Autowired
    private final BatchProcessing batchProcessing;
    private final RocksDBDao rocksDBDao;

    public BatchProcessController(BatchProcessing batchProcessing, RocksDBDao rocksDBDao) {
        this.batchProcessing = batchProcessing;
        this.rocksDBDao = rocksDBDao;
    }

    @ExceptionHandler(Exception.class)
    public Message error(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        return new Message(null, "error", ExceptionUtils.getStackTrace(e));
    }

    @RequestMapping(value = "/batch-job", method = RequestMethod.POST)
    public Message batchProcess() throws RocksDBException {
        this.batchProcessing.runBatchProcess();
        return new Message(
                "Batch Process Complete",
                null,
                null
        );
    }
}
