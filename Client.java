package pl.com.bottega.photostock.sales.model.users;

import pl.com.bottega.photostock.sales.model.deal.Money;
import pl.com.bottega.photostock.sales.model.deal.Offer;
import pl.com.bottega.photostock.sales.model.usertool.LightBox;

import java.util.ArrayList;
import java.util.List;


public class Client {

    String name;
    String address;
    private int debt;
    private String number;
    private Money saldo;
    private List<LightBox> lightboxes = new ArrayList();
    private List<Client> colleagues = new ArrayList();

    private PaymentStrategy paymentStrategy;

    public Client(String number, String name, String address, Money saldo, ClientStatus clientStatus) {
        this.address = address;
        this.name = name;
        this.saldo = saldo;
        this.number = number;
        this.paymentStrategy = new PaymentFactory().getPaymentStrategy(clientStatus);
    }

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public Money getSaldo() {
        return saldo;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return true;
    }

    public void agreeOffer(Offer offerToAgree) {
        offerToAgree.setAgreeByClient(true);
    }

    public boolean canAfford(Money offerAmount) {
        return paymentStrategy.canAfford(offerAmount);
    }

    public void charge(Money totalCost, String couse) {
        paymentStrategy.charge(totalCost, couse);
    }

    public void recharge(Money addMoney) {
        paymentStrategy.recharge(addMoney);
    }

    public enum FormattingLanguage {
        PL, EN;
    }

    public interface PaymentStrategy {
        boolean canAfford(Money offerAmount);

        void charge(Money offerAmount, String cause);

        void recharge(Money addMoney);
    }

    class PaymentFactory {
        public PaymentStrategy getPaymentStrategy(ClientStatus status) {

            PaymentStrategy paymentStrategy = null;

            switch (status) {
                case STANDARD:
                    paymentStrategy = new WithoutCredit();
                    break;
                case SILVER:
                    paymentStrategy = new WithoutCredit();
                    break;
                case VIP:
                    paymentStrategy = new CanUseCredit();
                    break;
                case PLATINUM:
                    paymentStrategy = new WithoutCredit();
                    break;
                case GOLD:
                    paymentStrategy = new WithoutCredit();
                    break;
                default:
                    throw new IllegalArgumentException("is not supported!");
            }

            return paymentStrategy;
        }
    }

    class CanUseCredit implements PaymentStrategy {

        @Override
        public boolean canAfford(Money offerAmount) {
            if (saldo > 0)
                return saldo + creditLimit >= offerAmount;
            else
                return creditLimit >= offerAmount;
        }

        @Override
        public void charge(Money offerAmount, String cause) {
            if (this.canAfford(offerAmount)) {
                if (amount >= offerAmount)
                    this.amount -= offerAmount;
                else {
                    debt = offerAmount - saldo;
                    creditLimit -= debt;
                    saldo = 0;
                }
            } else
                throw new IllegalStateException("Your count does not exist enough money");
        }

        @Override
        public void recharge(Money addMoney) {
            if (debt > 0) {
                if (addMoney.isBigger(debt)) {
                    saldo = addMoney - debt;
                    debt = 0;
                } else
                    debt -= addMoney;
            } else
                  amount += addMoney;
        }
    }

    class WithoutCredit implements PaymentStrategy {

        @Override
        public boolean canAfford(Money offerAmount) {
            return saldo.isBigger(offerAmount);
        }

        @Override
        public void charge(Money offerAmount, String cause) {
            if (this.canAfford(offerAmount))
                saldo.minus(offerAmount);
            else
                throw new IllegalStateException("Your count does not exist enough money");
        }

        @Override
        public void recharge(Money addMoney) {
            saldo.add(addMoney);
        }
    }
}