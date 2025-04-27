package nda.search.general.application.integration.conversion

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping(
    produces = [MediaType.APPLICATION_JSON_VALUE],
    consumes = [MediaType.APPLICATION_JSON_VALUE]
)
interface YfmConverterService {

    @PostMapping("/yfm2text")
    fun <T> yfm2text(rq: Map<T, String>): Map<T, String>

    @PostMapping("/yfm2html")
    fun <T> yfm2html(rq: Map<T, String>): Map<T, String>
}
