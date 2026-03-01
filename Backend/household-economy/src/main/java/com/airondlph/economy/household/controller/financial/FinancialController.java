package com.airondlph.economy.household.controller.financial;

import com.airondlph.economy.household.controller.data.Result;
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
    public Result<BankAccountVO> deleteBankAccountVO(UserVO userVO, BankAccountVO bankAccountVO);
    public Result<BankAccountVO> editBankAccountVO(UserVO userVO, BankAccountVO bankAccountVO);

    // public Result<List<UserVO>> getBankAccountOwnersVO(UserVO userVO, BankAccountVO bankAccountVO); // TODO
    // public Result<List<UserVO>> addBankAccountOwnerVO(UserVO userVO, BankAccountVO bankAccountVO, UserVO ownerVO); // TODO
    // public Result<List<UserVO>> removeBankAccountOwnerVO(UserVO userVO, BankAccountVO bankAccountVO, UserVO ownerVO); // TODO

    public Result<BankAccountCompleteVO> getBankAccountCompleteVO(UserVO userVO, BankAccountVO bankAccountVO);
    public Result<List<BankAccountVO>> getOwnerBankAccountsVO(UserVO userVO, UserVO ownerVO);

    // Credit Card
    public Result<CreditCardVO> createCreditCardVO(UserVO userVO, CreditCardVO creditCardVO);

    // Debit card

}
