package nda.search.general.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import nda.search.general.api.dto.ResponseStatusDto
import nda.search.general.api.dto.indexation.IndexationCreateRqDto
import nda.search.general.api.dto.indexation.IndexationInfoDto

@RestController
@RequestMapping(
    path = [
        "/api/indexation",
    ],
)
interface IndexationController {

    @PostMapping
    fun create(@RequestBody indexationCreateRqDto: IndexationCreateRqDto): IndexationInfoDto

    @GetMapping("/{indexationId}")
    fun get(@PathVariable("indexationId") indexationId: Long): IndexationInfoDto

    @PostMapping("/start/{indexationId}")
    fun startIndexation(@PathVariable("indexationId") indexationId: Long): IndexationInfoDto

    @PostMapping("/stop/{indexationId}")
    fun stopIndexation(@PathVariable("indexationId") indexationId: Long): IndexationInfoDto

    @PostMapping("/update/runners/{indexationId}")
    fun updateIndexationRunners(@PathVariable("indexationId") indexationId: Long): IndexationInfoDto

    @PostMapping("/init/rag/{ragIndexId}")
    fun initRagIndex(@PathVariable("ragIndexId") ragIndexId: Long): ResponseStatusDto
}
