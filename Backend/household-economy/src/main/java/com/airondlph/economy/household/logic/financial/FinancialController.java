package com.airondlph.economy.household.logic.financial;

import com.airondlph.economy.household.logic.data.Result;
import com.airondlph.economy.household.data.model.*;

import java.util.List;

public interface FinancialController {

    // Bank
    public Result<BankVO> createBankVO(UserVO userVO, BankVO bankVO);
    public Result<BankVO> getBankByIdVO(UserVO userVO, BankVO bankVO);
    public Result<BankVO> deleteBankByIdVO(UserVO userVO, BankVO bankVO);
    public Result<BankVO> editBankVO(UserVO userVO, BankVO bankVO);

    // Bank Account
    public Result<BankAccountVO> createBankAccountVO(UserVO userVO, BankAccountVO bankAccountVO, List<UserVO> ownersVO);
    public Result<BankAccountVO> deleteBankAccountByIdVO(UserVO userVO, BankAccountVO bankAccountVO);

    public Result<List<UserVO>> getBankAccountOwnersVO(UserVO userVO, BankAccountVO bankAccountVO); // TODO
    public Result<Void> addBankAccountOwnerVO(UserVO userVO, BankAccountVO bankAccountVO, UserVO ownerVO); // TODO
    public Result<Void> removeBankAccountOwnerVO(UserVO userVO, BankAccountVO bankAccountVO, UserVO ownerVO); // TODO

    public Result<BankAccountCompleteVO> getBankAccountCompleteVO(UserVO userVO, BankAccountVO bankAccountVO);
    public Result<List<BankAccountVO>> getOwnerBankAccountsVO(UserVO userVO, UserVO ownerVO);

    public Result<BankTransferVO> getBankTransferByIdVO(UserVO userVO, BankTransferVO bankTransferVO);
    public Result<BankTransferVO> createBankTransferVO(UserVO userVO, BankTransferVO bankTransferVO);

    // Credit Card
    public Result<CreditCardVO> getCreditCardByIdVO(UserVO userVO, CreditCardVO creditCardVO);
    public Result<CreditCardVO> createCreditCardVO(UserVO userVO, CreditCardVO creditCardVO);
    // public Result<CreditCardVO> deleteCreditCardVO(UserVO userVO, CreditCardVO creditCardVO);
    // public Result<CreditCardVO> editCreditCardVO(UserVO userVO, CreditCardVO creditCardVO);

    // Debit card

}
