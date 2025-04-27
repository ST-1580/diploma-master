package nda.search.general.web.api.myself

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nda.library.spring.boot.security.model.current.CurrentUserService

@RestController
@RequestMapping("/api")
class MyselfWebService @Autowired constructor(
    private val currentUserService: CurrentUserService,
) {

    @GetMapping("/myself")
    fun myself(): MyselfResponseDto {
        val current = currentUserService.authenticationOrNull()
        LOG.debug("Getting myself info for ${current?.let { "not null" } ?: "null"} authentication")
        return MyselfResponseDto(
            current?.user?.userIdentifier,
            current?.user?.authSystemIdentifier,
            current?.authority?.name,
        )
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(MyselfWebService::class.java)
    }
}
