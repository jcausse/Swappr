import ar.edu.itba.paw.model.Offer;
public class TestClass {
    public void test() {
        Offer offer = new Offer();
        java.util.Optional<ar.edu.itba.paw.model.File> f = offer.getProofOfPayment();
    }
}
