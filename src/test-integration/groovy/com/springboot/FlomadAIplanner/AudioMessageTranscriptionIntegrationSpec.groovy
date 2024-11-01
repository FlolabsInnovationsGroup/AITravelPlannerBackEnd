package com.springboot.FlomadAIplanner

import com.springboot.FlomadAIplanner.ai.agents.audio.AudioTranscriptionService
import com.springboot.FlomadAIplanner.config.GlobalSetupInitializer
import com.springboot.FlomadAIplanner.config.IntegrationTestsConfig
import com.springboot.FlomadAIplanner.config.MongoDBConfig
import com.springboot.FlomadAIplanner.model.entities.Session
import com.springboot.FlomadAIplanner.model.entities.login.User
import com.springboot.FlomadAIplanner.service.model.SessionService
import com.springboot.FlomadAIplanner.service.model.UserService
import com.springboot.FlomadAIplanner.util.ResponseEvaluator
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.hamcrest.Matchers.emptyOrNullString
import static org.hamcrest.Matchers.not
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


/**
 * Integration tests for the Audio Message Transcription feature
 *
 * Spock library is used for testing. doc: https://spockframework.org/spock/docs/1.3/all_in_one.html
 * The tests are run in the context of the Spring Boot application
 * The tests are run with an docker image MongoDB instance. So Docker must be installed on the machine.
 */
@Slf4j
@SpringBootTest
@ContextConfiguration(
        classes = [IntegrationTestsConfig, MongoDBConfig],
        initializers = [GlobalSetupInitializer]
)
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
class AudioMessageTranscriptionIntegrationSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    AudioTranscriptionService audioTranscriptionService

    @Autowired
    ResponseEvaluator responseEvaluator

    @Autowired
    UserService userService

    @Autowired
    SessionService sessionService

    Session session

    def setup() {
        // Setup resources
        User user = new User(username: 'testUser', password: 'testPassword')
        userService.save(user)

        session = new Session(title: 'testSession', userId: user.id)
        sessionService.save(session)
    }

    def cleanup() {
        // Cleanup resources
        sessionService.deleteAll()
        userService.deleteAll()
    }

    def "Test AiTravelAgent handling call with English audio"() {
        given: "An English audio file and a transcribed user question"
        def audioResource = new ClassPathResource("audio/test_request_eng.mp3")
        def mediaFile = new MockMultipartFile(
                "media",
                "test_request_eng.mp3",
                "audio/mpeg",
                audioResource.inputStream
        )

        def userQuestion = audioTranscriptionService.transcribeAudio(mediaFile)
        log.info("Transcribed User Question: ${userQuestion}")

        when: "The API endpoint is called with the audio file"
        def result = mockMvc.perform(multipart("/api/chat/call")
                .file(mediaFile)
                .param("sessionId", session.id)
                .param("message", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyOrNullString())))
                .andReturn()

        def responseContent = result.response.contentAsString
        log.info("Response Content: ${responseContent}")

        def isRelevant = responseEvaluator.isResponseRelevant(userQuestion, responseContent)
        log.info("Is Response Relevant: ${isRelevant}")

        then: "The response should be relevant to the question"
        assert isRelevant, "Response is not relevant to the question"
    }

    def "Test AiTravelAgent handling call with Different Language audio"() {
        given: "A Ukrainian audio file and a transcribed user question"
        def audioResource = new ClassPathResource("audio/test_request_ukr.mp3")
        def mediaFile = new MockMultipartFile(
                "media",
                "test_request_ukr.mp3",
                "audio/mpeg",
                audioResource.inputStream
        )

        def userQuestion = audioTranscriptionService.transcribeAudio(mediaFile)
        log.info("Transcribed User Question: ${userQuestion}")

        when: "The API endpoint is called with the audio file"
        def result = mockMvc.perform(multipart("/api/chat/call")
                .file(mediaFile)
                .param("sessionId", session.id)
                .param("message", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyOrNullString())))
                .andReturn()

        def responseContent = result.response.contentAsString
        log.info("Response Content: ${responseContent}")

        def isRelevant = responseEvaluator.isResponseRelevant(userQuestion, responseContent)
        log.info("Is Response Relevant: ${isRelevant}")

        then: "The response should be relevant to the question"
        assert isRelevant, "Response is not relevant to the question"
    }
}
