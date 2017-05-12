package ru.lanwen.raml.rarc;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.lanwen.raml.rarc.api.ApiResourceClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author lanwen (Merkushev Kirill)
 */
@RunWith(DataProviderRunner.class)
public class ApiResourceClassTest {

    @Test
    @DataProvider({
            "BundleV2,bundleV2",
            "2Bundle,_2Bundle",
            "bla_bla_bla,blaBlaBla",
            "api.bla_bla_bla,api.blaBlaBla",
    })
    public void shouldSanitize(String name, String expected) throws Exception {
        assertThat(ApiResourceClass.sanitize(name), equalTo(expected));
    }
}
