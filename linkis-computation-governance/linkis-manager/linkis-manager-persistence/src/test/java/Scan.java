import org.apache.linkis.DataWorkCloudApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


@ComponentScan(value = "org.apache.linkis", excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,value = DataWorkCloudApplication.class))
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
