package com.airondlph.economy.household.data.model;

import lombok.*;

import java.util.List;

/**
 * @author adriandlph / airondlph
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString(callSuper = true)
public class BankAccountCompleteVO extends BankAccountVO {

    private List<UserVO> ownersVO;

    public BankAccountCompleteVO(BankAccountVO bankAccountVO) {
        super(bankAccountVO);
    }

    public static BankAccountCompleteVOBuilder builder() {
        return new BankAccountCompleteVOBuilder();
    }

    public static class BankAccountCompleteVOBuilder extends BankAccountVOBuilder {
        private List<UserVO> ownersVO;

        public BankAccountCompleteVOBuilder() {
            super();
        }

        public BankAccountCompleteVOBuilder ownersVO(List<UserVO> ownersVO) {
            this.ownersVO = ownersVO;
            return this;
        }

        @Override
        public BankAccountCompleteVO build() {
            BankAccountCompleteVO result = (BankAccountCompleteVO) super.build();
            result.setOwnersVO(this.ownersVO);
            return result;
        }
    }

}
