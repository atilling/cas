<!-- fragment:keep -->

<p/>

#### Password Policy Strategies

If the password policy strategy is to be handed off to a Groovy script, the outline of the script may be as follows:

```groovy
import java.util.*
import org.ldaptive.auth.*
import org.apereo.cas.*
import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.support.*

List<MessageDescriptor> run(final Object... args) {
    def (response,configuration,logger,applicationContext) = args
    logger.info("Handling password policy [{}] via ${configuration.getAccountStateHandler()}", response)

    def accountStateHandler = configuration.getAccountStateHandler()
    return accountStateHandler.handle(response, configuration)
}
```

The parameters passed are as follows:

| Parameter            | Description                                                                         |
|----------------------|-------------------------------------------------------------------------------------|
| `response`           | The LDAP authentication response of type `org.ldaptive.auth.AuthenticationResponse` |
| `configuration`      | The LDAP password policy configuration carrying the account state handler defined.  |
| `logger`             | The object responsible for issuing log messages such as `logger.info(...)`.         |
| `applicationContext` | The Spring `ApplicationContext` that allows one to interact with the runtime.       |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide]({{ baseUrl }}/integration/Apache-Groovy-Scripting.html).
