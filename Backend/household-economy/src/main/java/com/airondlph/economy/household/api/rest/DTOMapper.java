package com.airondlph.economy.household.api.rest;

import com.airondlph.economy.household.api.rest.data.BankAccountDTO;
import com.airondlph.economy.household.api.rest.data.CreditCardDTO;
import com.airondlph.economy.household.api.rest.data.DebitCardDTO;
import com.airondlph.economy.household.api.rest.data.UserDTO;
import com.airondlph.economy.household.data.model.BankAccountVO;
import com.airondlph.economy.household.data.model.CreditCardVO;
import com.airondlph.economy.household.data.model.DebitCardVO;
import com.airondlph.economy.household.data.model.UserVO;

/**
 * @author adriandlph / airondlph
 */
public class DTOMapper {

    public static CreditCardDTO creditCardVO2creditCardDTO(CreditCardVO creditCardVO) {
        if(creditCardVO == null) return null;

        return CreditCardDTO.builder()
                .id(creditCardVO.getId())
                .cardNumber(creditCardVO.getCardNumber())
                .expires(creditCardVO.getExpires())
                .owner(creditCardVO.getOwnerVO() == null
                        ? null
                        : UserDTO.builder()
                        .id(creditCardVO.getOwnerVO().getId())
                        .firstName(creditCardVO.getOwnerVO().getFirstName())
                        .build())
                .bankAccount(creditCardVO.getBankAccountVO() == null
                        ? null
                        : BankAccountDTO.builder()
                        .id(creditCardVO.getBankAccountVO().getId())
                        .build())
                .build();
    }

    public static DebitCardDTO debitCardVO2debitCardDTO(DebitCardVO debitCardVO) {
        if(debitCardVO == null) return null;

        return (DebitCardDTO) DebitCardDTO.builder()
                .id(debitCardVO.getId())
                .cardNumber(debitCardVO.getCardNumber())
                .expires(debitCardVO.getExpires())
                .owner(debitCardVO.getOwnerVO() == null
                        ? null
                        : UserDTO.builder()
                        .id(debitCardVO.getOwnerVO().getId())
                        .firstName(debitCardVO.getOwnerVO().getFirstName())
                        .build())
                .bankAccount(debitCardVO.getBankAccountVO() == null
                        ? null
                        : BankAccountDTO.builder()
                        .id(debitCardVO.getBankAccountVO().getId())
                        .build())
                .build();
    }

    public static CreditCardVO creditCardDTO2creditCardVO(CreditCardDTO creditCardDTO) {
        return creditCardDTO == null
                ? null
                : CreditCardVO.builder()
                .id(creditCardDTO.getId())
                .cardNumber(creditCardDTO.getCardNumber())
                .ccv(creditCardDTO.getCcv())
                .pin(creditCardDTO.getPin())
                .expires(creditCardDTO.getExpires())
                .ownerVO(
                        creditCardDTO.getOwner() == null
                                ? null
                                : UserVO.builder()
                                .id(creditCardDTO.getOwner().getId())
                                .build())
                .bankAccountVO(creditCardDTO.getBankAccount() == null
                        ? null
                        : BankAccountVO.builder()
                        .id(creditCardDTO.getBankAccount().getId())
                        .build())
                .build();
    }

}
