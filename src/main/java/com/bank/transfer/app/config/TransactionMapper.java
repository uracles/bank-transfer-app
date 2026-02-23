package com.bank.transfer.app.config;


import com.bank.transfer.app.dto.TransactionSummaryResponse;
import com.bank.transfer.app.dto.TransferResponse;
import com.bank.transfer.app.entity.Transaction;
import com.bank.transfer.app.entity.TransactionSummary;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {

    TransferResponse toTransferResponse(Transaction transaction);

    TransactionSummaryResponse toSummaryResponse(TransactionSummary summary);
}
