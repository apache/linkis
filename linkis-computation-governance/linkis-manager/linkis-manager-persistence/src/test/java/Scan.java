import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Created by v_wbjftang on 2020/2/13.
 */
@ComponentScan(value = "com.webank.wedatasphere", excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,value = DataWorkCloudApplication.class))
@Configuration
@EnableAspectJAutoProxy
public class Scan {
    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

}
