package nda.search.general.web.api.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import nda.search.general.application.exception.IndexationException
import ru.nda.library.spring.boot.security.model.current.CurrentUserService

@Service
class UserService(
    private val currentUserService: CurrentUserService,

    @Value("\${nda.search.general.indexation.api.whitelist:st1580}")
    private val usersWhiteList: Set<String>,

    @Value("\${nda.search.general.indexation.api.whitelist.enabled:true}")
    private val isWhiteListEnabled: Boolean,
) {

    fun checkUser() {
        if (!isWhiteListEnabled) {
            return
        }

        val userLogin = currentUserService.userOrThrow().userIdentifier

        if (!usersWhiteList.contains(userLogin)) {
            throw IndexationException.NotAvailableOperationException(userLogin)
        }
    }
}
