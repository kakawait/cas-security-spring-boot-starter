import com.kakawait.spring.security.cas.client.CasRequestSpecification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jonathan Coueraud
 */
public class CasRequestSpecificationTest {
    private CasRequestSpecification casRequestSpecification;

    @Before
    public void setup() {
        casRequestSpecification = new CasRequestSpecification();
    }

    @After
    public void tearDown() {
        casRequestSpecification = null;
    }

    @Test
    public void testDoItNeedProxyTicket() {
        assertThat(casRequestSpecification.doItNeedProxyTicket(null));
    }
}
