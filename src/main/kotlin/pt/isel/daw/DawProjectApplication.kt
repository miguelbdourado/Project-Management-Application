package pt.isel.daw

import com.fasterxml.jackson.annotation.JsonInclude
import javax.sql.DataSource
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import pt.isel.daw.control.authorization.verifyUserCredentials
import pt.isel.daw.control.config.ConfigProperties
import pt.isel.daw.model.Profile

@SpringBootApplication
@ConfigurationPropertiesScan
class DawProjectApplication(
    private val configProperties: ConfigProperties
) {
    @Bean
    fun jackson2ObjectMapperBuilder() = Jackson2ObjectMapperBuilder()
        .serializationInclusion(JsonInclude.Include.NON_NULL)

    @Bean
    fun dataSource() = PGSimpleDataSource().apply {
        setURL(configProperties.dbConnString)
    }

    @Bean
    fun jdbi(dataSource: DataSource): Jdbi = Jdbi.create(dataSource).apply {
        installPlugin(KotlinPlugin())
    }

    @Bean
    fun authenticationProvider(): (String, Jdbi) -> Profile? = ::verifyUserCredentials

}

fun main(args: Array<String>) {
    runApplication<DawProjectApplication>(*args)
}


