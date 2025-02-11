package com.springboot.FlomadAIplanner.ai.client;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.client.advisor.DefaultAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.client.observation.ChatClientObservationContext;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.client.observation.ChatClientObservationDocumentation;
import org.springframework.ai.chat.client.observation.DefaultChatClientObservationConvention;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.ai.model.Media;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Custom CallResponseSpec that uses CustomBeanOutputConverter.
 */
@Slf4j
@SuppressWarnings("all")
public class CustomChatClient implements ChatClient {

    private static final ChatClientObservationConvention DEFAULT_CHAT_CLIENT_OBSERVATION_CONVENTION = new DefaultChatClientObservationConvention();

    private final CustomChatClient.DefaultChatClientRequestSpec defaultChatClientRequest;

    public CustomChatClient(CustomChatClient.DefaultChatClientRequestSpec defaultChatClientRequest) {
        Assert.notNull(defaultChatClientRequest, "defaultChatClientRequest cannot be null");
        this.defaultChatClientRequest = defaultChatClientRequest;
    }

    private static AdvisedRequest toAdvisedRequest(CustomChatClient.DefaultChatClientRequestSpec inputRequest,
                                                   @Nullable String formatParam) {
        Assert.notNull(inputRequest, "inputRequest cannot be null");

        Map<String, Object> advisorContext = new ConcurrentHashMap<>(inputRequest.getAdvisorParams());
        if (StringUtils.hasText(formatParam)) {
            advisorContext.put("formatParam", formatParam);
        }

        // Process userText, media and messages before creating the AdvisedRequest.
        String userText = inputRequest.userText;
        List<Media> media = inputRequest.media;
        List<Message> messages = inputRequest.messages;

        // If the userText is empty, then try extracting the userText from the last
        // message
        // in the messages list and remove it from the messages list.
        if (!StringUtils.hasText(userText) && !CollectionUtils.isEmpty(messages)) {
            Message lastMessage = messages.get(messages.size() - 1);
            if (lastMessage.getMessageType() == MessageType.USER) {
                UserMessage userMessage = (UserMessage) lastMessage;
                if (StringUtils.hasText(userMessage.getContent())) {
                    userText = lastMessage.getContent();
                }
                Collection<Media> messageMedia = userMessage.getMedia();
                if (!CollectionUtils.isEmpty(messageMedia)) {
                    media.addAll(messageMedia);
                }
                messages = messages.subList(0, messages.size() - 1);
            }
        }

        return new AdvisedRequest(inputRequest.chatModel, userText, inputRequest.systemText, inputRequest.chatOptions,
                media, inputRequest.functionNames, inputRequest.functionCallbacks, messages, inputRequest.userParams,
                inputRequest.systemParams, inputRequest.advisors, inputRequest.advisorParams, advisorContext,
                inputRequest.toolContext);
    }

    public static CustomChatClient.DefaultChatClientRequestSpec toDefaultChatClientRequestSpec(AdvisedRequest advisedRequest,
                                                                                               ObservationRegistry observationRegistry, ChatClientObservationConvention customObservationConvention) {

        return new CustomChatClient.DefaultChatClientRequestSpec(advisedRequest.chatModel(), advisedRequest.userText(),
                advisedRequest.userParams(), advisedRequest.systemText(), advisedRequest.systemParams(),
                advisedRequest.functionCallbacks(), advisedRequest.messages(), advisedRequest.functionNames(),
                advisedRequest.media(), advisedRequest.chatOptions(), advisedRequest.advisors(),
                advisedRequest.advisorParams(), observationRegistry, customObservationConvention,
                advisedRequest.toolContext());
    }

    public static Builder builder(ChatModel chatModel) {
        return builder(chatModel, ObservationRegistry.NOOP, null);
    }

    static Builder builder(ChatModel chatModel, ObservationRegistry observationRegistry,
                           @Nullable ChatClientObservationConvention customObservationConvention) {
        Assert.notNull(chatModel, "chatModel cannot be null");
        Assert.notNull(observationRegistry, "observationRegistry cannot be null");
        return new CustomChatClientBuilder(chatModel, observationRegistry, customObservationConvention);
    }

    @Override
    public ChatClientRequestSpec prompt() {
        return new CustomChatClient.DefaultChatClientRequestSpec(this.defaultChatClientRequest);
    }

    @Override
    public ChatClientRequestSpec prompt(String content) {
        Assert.hasText(content, "content cannot be null or empty");
        return prompt(new Prompt(content));
    }

    @Override
    public ChatClientRequestSpec prompt(Prompt prompt) {
        Assert.notNull(prompt, "prompt cannot be null");

        CustomChatClient.DefaultChatClientRequestSpec spec = new CustomChatClient.DefaultChatClientRequestSpec(this.defaultChatClientRequest);

        // Options
        if (prompt.getOptions() != null) {
            spec.options(prompt.getOptions());
        }

        // Messages
        if (prompt.getInstructions() != null) {
            spec.messages(prompt.getInstructions());
        }

        return spec;
    }

    /**
     * Return a {@code ChatClient2Builder} to create a new {@code ChatClient} whose
     * settings are replicated from this {@code ChatClientRequest}.
     */
    @Override
    public Builder mutate() {
        return this.defaultChatClientRequest.mutate();
    }

    public static class DefaultPromptUserSpec implements PromptUserSpec {

        private final Map<String, Object> params = new HashMap<>();

        private final List<Media> media = new ArrayList<>();

        @Nullable
        private String text;

        @Override
        public PromptUserSpec media(Media... media) {
            Assert.notNull(media, "media cannot be null");
            Assert.noNullElements(media, "media cannot contain null elements");
            this.media.addAll(Arrays.asList(media));
            return this;
        }

        @Override
        public PromptUserSpec media(MimeType mimeType, URL url) {
            Assert.notNull(mimeType, "mimeType cannot be null");
            Assert.notNull(url, "url cannot be null");
            this.media.add(new Media(mimeType, url));
            return this;
        }

        @Override
        public PromptUserSpec media(MimeType mimeType, Resource resource) {
            Assert.notNull(mimeType, "mimeType cannot be null");
            Assert.notNull(resource, "resource cannot be null");
            this.media.add(new Media(mimeType, resource));
            return this;
        }

        @Override
        public PromptUserSpec text(String text) {
            Assert.hasText(text, "text cannot be null or empty");
            this.text = text;
            return this;
        }

        @Override
        public PromptUserSpec text(Resource text, Charset charset) {
            Assert.notNull(text, "text cannot be null");
            Assert.notNull(charset, "charset cannot be null");
            try {
                this.text(text.getContentAsString(charset));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
        public PromptUserSpec text(Resource text) {
            Assert.notNull(text, "text cannot be null");
            this.text(text, Charset.defaultCharset());
            return this;
        }

        @Override
        public PromptUserSpec param(String key, Object value) {
            Assert.hasText(key, "key cannot be null or empty");
            Assert.notNull(value, "value cannot be null");
            this.params.put(key, value);
            return this;
        }

        @Override
        public PromptUserSpec params(Map<String, Object> params) {
            Assert.notNull(params, "params cannot be null");
            Assert.noNullElements(params.keySet(), "param keys cannot contain null elements");
            Assert.noNullElements(params.values(), "param values cannot contain null elements");
            this.params.putAll(params);
            return this;
        }

        @Nullable
        protected String text() {
            return this.text;
        }

        protected Map<String, Object> params() {
            return this.params;
        }

        protected List<Media> media() {
            return this.media;
        }

    }

    public static class DefaultPromptSystemSpec implements PromptSystemSpec {

        private final Map<String, Object> params = new HashMap<>();

        @Nullable
        private String text;

        @Override
        public PromptSystemSpec text(String text) {
            Assert.hasText(text, "text cannot be null or empty");
            this.text = text;
            return this;
        }

        @Override
        public PromptSystemSpec text(Resource text, Charset charset) {
            Assert.notNull(text, "text cannot be null");
            Assert.notNull(charset, "charset cannot be null");
            try {
                this.text(text.getContentAsString(charset));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
        public PromptSystemSpec text(Resource text) {
            Assert.notNull(text, "text cannot be null");
            this.text(text, Charset.defaultCharset());
            return this;
        }

        @Override
        public PromptSystemSpec param(String key, Object value) {
            Assert.hasText(key, "key cannot be null or empty");
            Assert.notNull(value, "value cannot be null");
            this.params.put(key, value);
            return this;
        }

        @Override
        public PromptSystemSpec params(Map<String, Object> params) {
            Assert.notNull(params, "params cannot be null");
            Assert.noNullElements(params.keySet(), "param keys cannot contain null elements");
            Assert.noNullElements(params.values(), "param values cannot contain null elements");
            this.params.putAll(params);
            return this;
        }

        @Nullable
        protected String text() {
            return this.text;
        }

        protected Map<String, Object> params() {
            return this.params;
        }

    }

    public static class DefaultAdvisorSpec implements AdvisorSpec {

        private final List<Advisor> advisors = new ArrayList<>();

        private final Map<String, Object> params = new HashMap<>();

        @Override
        public AdvisorSpec param(String key, Object value) {
            Assert.hasText(key, "key cannot be null or empty");
            Assert.notNull(value, "value cannot be null");
            this.params.put(key, value);
            return this;
        }

        @Override
        public AdvisorSpec params(Map<String, Object> params) {
            Assert.notNull(params, "params cannot be null");
            Assert.noNullElements(params.keySet(), "param keys cannot contain null elements");
            Assert.noNullElements(params.values(), "param values cannot contain null elements");
            this.params.putAll(params);
            return this;
        }

        @Override
        public AdvisorSpec advisors(Advisor... advisors) {
            Assert.notNull(advisors, "advisors cannot be null");
            Assert.noNullElements(advisors, "advisors cannot contain null elements");
            this.advisors.addAll(List.of(advisors));
            return this;
        }

        @Override
        public AdvisorSpec advisors(List<Advisor> advisors) {
            Assert.notNull(advisors, "advisors cannot be null");
            Assert.noNullElements(advisors, "advisors cannot contain null elements");
            this.advisors.addAll(advisors);
            return this;
        }

        public List<Advisor> getAdvisors() {
            return this.advisors;
        }

        public Map<String, Object> getParams() {
            return this.params;
        }

    }

    public static class DefaultCallResponseSpec implements CallResponseSpec {

        private final CustomChatClient.DefaultChatClientRequestSpec request;

        public DefaultCallResponseSpec(CustomChatClient.DefaultChatClientRequestSpec request) {
            Assert.notNull(request, "request cannot be null");
            this.request = request;
        }

        @Nullable
        private static String getContentFromChatResponse(@Nullable ChatResponse chatResponse) {
            if (chatResponse == null || chatResponse.getResult() == null || chatResponse.getResult().getOutput() == null
                    || chatResponse.getResult().getOutput().getContent() == null) {
                return null;
            }
            return chatResponse.getResult().getOutput().getContent();
        }

        @Override
        public <T> ResponseEntity<ChatResponse, T> responseEntity(Class<T> type) {
            Assert.notNull(type, "type cannot be null");
            return doResponseEntity(new BeanOutputConverter<T>(type));
        }

        @Override
        public <T> ResponseEntity<ChatResponse, T> responseEntity(ParameterizedTypeReference<T> type) {
            Assert.notNull(type, "type cannot be null");
            return doResponseEntity(new BeanOutputConverter<T>(type));
        }

        @Override
        public <T> ResponseEntity<ChatResponse, T> responseEntity(
                StructuredOutputConverter<T> structuredOutputConverter) {
            Assert.notNull(structuredOutputConverter, "structuredOutputConverter cannot be null");
            return doResponseEntity(structuredOutputConverter);
        }

        protected <T> ResponseEntity<ChatResponse, T> doResponseEntity(StructuredOutputConverter<T> outputConverter) {
            Assert.notNull(outputConverter, "structuredOutputConverter cannot be null");
            var chatResponse = doGetObservableChatResponse(this.request, outputConverter.getFormat());
            var responseContent = getContentFromChatResponse(chatResponse);
            if (responseContent == null) {
                return new ResponseEntity<>(chatResponse, null);
            }
            T entity = outputConverter.convert(responseContent);
            return new ResponseEntity<>(chatResponse, entity);
        }

        @Override
        @Nullable
        public <T> T entity(ParameterizedTypeReference<T> type) {
            Assert.notNull(type, "type cannot be null");
            return doSingleWithBeanOutputConverter(new BeanOutputConverter<>(type));
        }

        @Override
        @Nullable
        public <T> T entity(StructuredOutputConverter<T> structuredOutputConverter) {
            Assert.notNull(structuredOutputConverter, "structuredOutputConverter cannot be null");
            return doSingleWithBeanOutputConverter(structuredOutputConverter);
        }

        @Override
        @Nullable
        public <T> T entity(Class<T> type) {
            Assert.notNull(type, "type cannot be null");
            var outputConverter = new CustomBeanOutputConverter<>(type);
            return doSingleWithBeanOutputConverter(outputConverter);
        }

        @Nullable
        private <T> T doSingleWithBeanOutputConverter(StructuredOutputConverter<T> outputConverter) {
            var chatResponse = doGetObservableChatResponse(this.request, outputConverter.getFormat());
            var stringResponse = getContentFromChatResponse(chatResponse);
            if (stringResponse == null) {
                return null;
            }
            return outputConverter.convert(stringResponse);
        }

        @Nullable
        private ChatResponse doGetChatResponse() {
            return this.doGetObservableChatResponse(this.request, null);
        }

        @Nullable
        private ChatResponse doGetObservableChatResponse(CustomChatClient.DefaultChatClientRequestSpec inputRequest,
                                                         @Nullable String formatParam) {

            ChatClientObservationContext observationContext = ChatClientObservationContext.builder()
                    .withRequest(inputRequest)
                    .withFormat(formatParam)
                    .withStream(false)
                    .build();

            var observation = ChatClientObservationDocumentation.AI_CHAT_CLIENT.observation(
                    inputRequest.getCustomObservationConvention(), DEFAULT_CHAT_CLIENT_OBSERVATION_CONVENTION,
                    () -> observationContext, inputRequest.getObservationRegistry());
            return observation.observe(() -> doGetChatResponse(inputRequest, formatParam, observation));
        }

        private ChatResponse doGetChatResponse(CustomChatClient.DefaultChatClientRequestSpec inputRequestSpec,
                                               @Nullable String formatParam, Observation parentObservation) {

            AdvisedRequest advisedRequest = toAdvisedRequest(inputRequestSpec, formatParam);

            // Apply the around advisor chain that terminates with the last model call
            // advisor.
            AdvisedResponse advisedResponse = inputRequestSpec.aroundAdvisorChainBuilder.build()
                    .nextAroundCall(advisedRequest);

            return advisedResponse.response();
        }

        @Override
        @Nullable
        public ChatResponse chatResponse() {
            return doGetChatResponse();
        }

        @Override
        @Nullable
        public String content() {
            ChatResponse chatResponse = doGetChatResponse();
            return getContentFromChatResponse(chatResponse);
        }

    }

    public static class DefaultStreamResponseSpec implements StreamResponseSpec {

        private final CustomChatClient.DefaultChatClientRequestSpec request;

        public DefaultStreamResponseSpec(CustomChatClient.DefaultChatClientRequestSpec request) {
            Assert.notNull(request, "request cannot be null");
            this.request = request;
        }

        private Flux<ChatResponse> doGetObservableFluxChatResponse(CustomChatClient.DefaultChatClientRequestSpec inputRequest) {
            return Flux.deferContextual(contextView -> {

                ChatClientObservationContext observationContext = ChatClientObservationContext.builder()
                        .withRequest(inputRequest)
                        .withStream(true)
                        .build();

                Observation observation = ChatClientObservationDocumentation.AI_CHAT_CLIENT.observation(
                        inputRequest.getCustomObservationConvention(), DEFAULT_CHAT_CLIENT_OBSERVATION_CONVENTION,
                        () -> observationContext, inputRequest.getObservationRegistry());

                observation.parentObservation(contextView.getOrDefault(ObservationThreadLocalAccessor.KEY, null))
                        .start();

                var initialAdvisedRequest = toAdvisedRequest(inputRequest, null);

                // @formatter:off
                // Apply the around advisor chain that terminates with the last model call advisor.
                Flux<AdvisedResponse> stream = inputRequest.aroundAdvisorChainBuilder.build().nextAroundStream(initialAdvisedRequest);

                return stream
                        .map(AdvisedResponse::response)
                        .doOnError(observation::error)
                        .doFinally(s -> observation.stop())
                        .contextWrite(ctx -> ctx.put(ObservationThreadLocalAccessor.KEY, observation));
                // @formatter:on
            });
        }

        @Override
        public Flux<ChatResponse> chatResponse() {
            return doGetObservableFluxChatResponse(this.request);
        }

        @Override
        public Flux<String> content() {
            return doGetObservableFluxChatResponse(this.request).map(r -> {
                if (r.getResult() == null || r.getResult().getOutput() == null
                        || r.getResult().getOutput().getContent() == null) {
                    return "";
                }
                return r.getResult().getOutput().getContent();
            }).filter(StringUtils::hasLength);
        }

    }

    public static class DefaultChatClientRequestSpec extends DefaultChatClient.DefaultChatClientRequestSpec implements ChatClientRequestSpec {

        private final ObservationRegistry observationRegistry;

        private final ChatClientObservationConvention customObservationConvention;

        private final ChatModel chatModel;

        @Getter
        private final List<Media> media = new ArrayList<>();

        @Getter
        private final List<String> functionNames = new ArrayList<>();

        @Getter
        private final List<FunctionCallback> functionCallbacks = new ArrayList<>();

        @Getter
        private final List<Message> messages = new ArrayList<>();

        @Getter
        private final Map<String, Object> userParams = new HashMap<>();

        @Getter
        private final Map<String, Object> systemParams = new HashMap<>();

        @Getter
        private final List<Advisor> advisors = new ArrayList<>();

        @Getter
        private final Map<String, Object> advisorParams = new HashMap<>();

        private final DefaultAroundAdvisorChain.Builder aroundAdvisorChainBuilder;

        @Getter
        private final Map<String, Object> toolContext = new HashMap<>();

        @Nullable
        private String userText;

        @Nullable
        private String systemText;

        @Nullable
        private ChatOptions chatOptions;

        /* copy constructor */
        DefaultChatClientRequestSpec(CustomChatClient.DefaultChatClientRequestSpec ccr) {
            this(ccr.chatModel, ccr.userText, ccr.userParams, ccr.systemText, ccr.systemParams, ccr.functionCallbacks,
                    ccr.messages, ccr.functionNames, ccr.media, ccr.chatOptions, ccr.advisors, ccr.advisorParams,
                    ccr.observationRegistry, ccr.customObservationConvention, ccr.toolContext);
        }

        public DefaultChatClientRequestSpec(ChatModel chatModel, @Nullable String userText,
                                            Map<String, Object> userParams, @Nullable String systemText, Map<String, Object> systemParams,
                                            List<FunctionCallback> functionCallbacks, List<Message> messages, List<String> functionNames,
                                            List<Media> media, @Nullable ChatOptions chatOptions, List<Advisor> advisors,
                                            Map<String, Object> advisorParams, ObservationRegistry observationRegistry,
                                            @Nullable ChatClientObservationConvention customObservationConvention,
                                            Map<String, Object> toolContext) {
            super(chatModel, userText, userParams, systemText, systemParams, functionCallbacks, messages, functionNames,
                    media, chatOptions, advisors, advisorParams, observationRegistry, customObservationConvention, toolContext);

            Assert.notNull(chatModel, "chatModel cannot be null");
            Assert.notNull(userParams, "userParams cannot be null");
            Assert.notNull(systemParams, "systemParams cannot be null");
            Assert.notNull(functionCallbacks, "functionCallbacks cannot be null");
            Assert.notNull(messages, "messages cannot be null");
            Assert.notNull(functionNames, "functionNames cannot be null");
            Assert.notNull(media, "media cannot be null");
            Assert.notNull(advisors, "advisors cannot be null");
            Assert.notNull(advisorParams, "advisorParams cannot be null");
            Assert.notNull(observationRegistry, "observationRegistry cannot be null");
            Assert.notNull(toolContext, "toolContext cannot be null");

            this.chatModel = chatModel;
            this.chatOptions = chatOptions != null ? chatOptions.copy()
                    : (chatModel.getDefaultOptions() != null) ? chatModel.getDefaultOptions().copy() : null;

            this.userText = userText;
            this.userParams.putAll(userParams);
            this.systemText = systemText;
            this.systemParams.putAll(systemParams);

            this.functionNames.addAll(functionNames);
            this.functionCallbacks.addAll(functionCallbacks);
            this.messages.addAll(messages);
            this.media.addAll(media);
            this.advisors.addAll(advisors);
            this.advisorParams.putAll(advisorParams);
            this.observationRegistry = observationRegistry;
            this.customObservationConvention = customObservationConvention != null ? customObservationConvention
                    : DEFAULT_CHAT_CLIENT_OBSERVATION_CONVENTION;
            this.toolContext.putAll(toolContext);

            // @formatter:off
            // At the stack bottom add the non-streaming and streaming model call advisors.
            // They play the role of the last advisor in the around advisor chain.
            this.advisors.add(new CallAroundAdvisor() {

                @Override
                public String getName() {
                    return CallAroundAdvisor.class.getSimpleName();
                }

                @Override
                public int getOrder() {
                    return Ordered.LOWEST_PRECEDENCE;
                }

                @Override
                public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
                    return new AdvisedResponse(chatModel.call(advisedRequest.toPrompt()), Collections.unmodifiableMap(advisedRequest.adviseContext()));
                }
            });

            this.advisors.add(new StreamAroundAdvisor() {

                @Override
                public String getName() {
                    return StreamAroundAdvisor.class.getSimpleName();
                }

                @Override
                public int getOrder() {
                    return Ordered.LOWEST_PRECEDENCE;
                }

                @Override
                public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
                    return chatModel.stream(advisedRequest.toPrompt())
                            .map(chatResponse -> new AdvisedResponse(chatResponse, Collections.unmodifiableMap(advisedRequest.adviseContext())))
                            .publishOn(Schedulers.boundedElastic()); // TODO add option to disable.
                }
            });
            // @formatter:on

            this.aroundAdvisorChainBuilder = DefaultAroundAdvisorChain.builder(observationRegistry)
                    .pushAll(this.advisors);
        }

        private ObservationRegistry getObservationRegistry() {
            return this.observationRegistry;
        }

        private ChatClientObservationConvention getCustomObservationConvention() {
            return this.customObservationConvention;
        }

        @Nullable
        public String getUserText() {
            return this.userText;
        }

        @Nullable
        public String getSystemText() {
            return this.systemText;
        }

        @Nullable
        public ChatOptions getChatOptions() {
            return this.chatOptions;
        }

        /**
         * Return a {@code ChatClient2Builder} to create a new {@code ChatClient2} whose
         * settings are replicated from this {@code ChatClientRequest}.
         */
        public Builder mutate() {
            CustomChatClientBuilder builder = (CustomChatClientBuilder) ChatClient
                    .builder(this.chatModel)
                    .defaultFunctions(StringUtils.toStringArray(this.functionNames));

            if (StringUtils.hasText(this.userText)) {
                builder.defaultUser(
                        u -> u.text(this.userText).params(this.userParams).media(this.media.toArray(new Media[0])));
            }

            if (StringUtils.hasText(this.systemText)) {
                builder.defaultSystem(s -> s.text(this.systemText).params(this.systemParams));
            }

            if (this.chatOptions != null) {
                builder.defaultOptions(this.chatOptions);
            }

            // workaround to set the missing fields.
            builder.defaultRequest.getMessages().addAll(this.messages);
            builder.defaultRequest.getFunctionCallbacks().addAll(this.functionCallbacks);
            builder.defaultRequest.getToolContext().putAll(this.toolContext);

            return builder;
        }

        public ChatClientRequestSpec advisors(Consumer<AdvisorSpec> consumer) {
            Assert.notNull(consumer, "consumer cannot be null");
            var advisorSpec = new CustomChatClient.DefaultAdvisorSpec();
            consumer.accept(advisorSpec);
            this.advisorParams.putAll(advisorSpec.getParams());
            this.advisors.addAll(advisorSpec.getAdvisors());
            this.aroundAdvisorChainBuilder.pushAll(advisorSpec.getAdvisors());
            return this;
        }

        public ChatClientRequestSpec advisors(Advisor... advisors) {
            Assert.notNull(advisors, "advisors cannot be null");
            Assert.noNullElements(advisors, "advisors cannot contain null elements");
            this.advisors.addAll(Arrays.asList(advisors));
            this.aroundAdvisorChainBuilder.pushAll(Arrays.asList(advisors));
            return this;
        }

        public ChatClientRequestSpec advisors(List<Advisor> advisors) {
            Assert.notNull(advisors, "advisors cannot be null");
            Assert.noNullElements(advisors, "advisors cannot contain null elements");
            this.advisors.addAll(advisors);
            this.aroundAdvisorChainBuilder.pushAll(advisors);
            return this;
        }

        public ChatClientRequestSpec messages(Message... messages) {
            Assert.notNull(messages, "messages cannot be null");
            Assert.noNullElements(messages, "messages cannot contain null elements");
            this.messages.addAll(List.of(messages));
            return this;
        }

        public ChatClientRequestSpec messages(List<Message> messages) {
            Assert.notNull(messages, "messages cannot be null");
            Assert.noNullElements(messages, "messages cannot contain null elements");
            this.messages.addAll(messages);
            return this;
        }

        public <T extends ChatOptions> ChatClientRequestSpec options(T options) {
            Assert.notNull(options, "options cannot be null");
            this.chatOptions = options;
            return this;
        }

        @Override
        public <I, O> ChatClientRequestSpec function(String name, String description,
                                                     java.util.function.Function<I, O> function) {
            Assert.hasText(name, "name cannot be null or empty");
            Assert.hasText(description, "description cannot be null or empty");
            Assert.notNull(function, "function cannot be null");

            var fcw = FunctionCallbackWrapper.builder(function)
                    .withDescription(description)
                    .withName(name)
                    .withResponseConverter(Object::toString)
                    .build();
            this.functionCallbacks.add(fcw);
            return this;
        }

        @Override
        public <I, O> ChatClientRequestSpec function(String name, String description,
                                                     java.util.function.BiFunction<I, ToolContext, O> biFunction) {

            Assert.hasText(name, "name cannot be null or empty");
            Assert.hasText(description, "description cannot be null or empty");
            Assert.notNull(biFunction, "biFunction cannot be null");

            var fcw = FunctionCallbackWrapper.builder(biFunction)
                    .withDescription(description)
                    .withName(name)
                    .withResponseConverter(Object::toString)
                    .build();
            this.functionCallbacks.add(fcw);
            return this;
        }

        @Override
        public <I, O> ChatClientRequestSpec function(String name, String description, @Nullable Class<I> inputType,
                                                     java.util.function.Function<I, O> function) {

            Assert.hasText(name, "name cannot be null or empty");
            Assert.hasText(description, "description cannot be null or empty");
            Assert.notNull(function, "function cannot be null");

            var fcw = FunctionCallback.builder()
//                    .description(description)
//                    .responseConverter(Object::toString)
                    .function(name, function)
                    .inputType(inputType)
                    .build();
            this.functionCallbacks.add(fcw);
            return this;
        }

        public ChatClientRequestSpec functions(String... functionBeanNames) {
            Assert.notNull(functionBeanNames, "functionBeanNames cannot be null");
            Assert.noNullElements(functionBeanNames, "functionBeanNames cannot contain null elements");
            this.functionNames.addAll(List.of(functionBeanNames));
            return this;
        }

        public ChatClientRequestSpec functions(FunctionCallback... functionCallbacks) {
            Assert.notNull(functionCallbacks, "functionCallbacks cannot be null");
            Assert.noNullElements(functionCallbacks, "functionCallbacks cannot contain null elements");
            this.functionCallbacks.addAll(Arrays.asList(functionCallbacks));
            return this;
        }

        public ChatClientRequestSpec toolContext(Map<String, Object> toolContext) {
            Assert.notNull(toolContext, "toolContext cannot be null");
            Assert.noNullElements(toolContext.keySet(), "toolContext keys cannot contain null elements");
            Assert.noNullElements(toolContext.values(), "toolContext values cannot contain null elements");
            this.toolContext.putAll(toolContext);
            return this;
        }

        public ChatClientRequestSpec system(String text) {
            Assert.hasText(text, "text cannot be null or empty");
            this.systemText = text;
            return this;
        }

        public ChatClientRequestSpec system(Resource text, Charset charset) {
            Assert.notNull(text, "text cannot be null");
            Assert.notNull(charset, "charset cannot be null");

            try {
                this.systemText = text.getContentAsString(charset);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public ChatClientRequestSpec system(Resource text) {
            Assert.notNull(text, "text cannot be null");
            return this.system(text, Charset.defaultCharset());
        }

        public ChatClientRequestSpec system(Consumer<PromptSystemSpec> consumer) {
            Assert.notNull(consumer, "consumer cannot be null");

            var systemSpec = new CustomChatClient.DefaultPromptSystemSpec();
            consumer.accept(systemSpec);
            this.systemText = StringUtils.hasText(systemSpec.text()) ? systemSpec.text() : this.systemText;
            this.systemParams.putAll(systemSpec.params());

            return this;
        }

        public ChatClientRequestSpec user(String text) {
            Assert.hasText(text, "text cannot be null or empty");
            this.userText = text;
            return this;
        }

        public ChatClientRequestSpec user(Resource text, Charset charset) {
            Assert.notNull(text, "text cannot be null");
            Assert.notNull(charset, "charset cannot be null");

            try {
                this.userText = text.getContentAsString(charset);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public ChatClientRequestSpec user(Resource text) {
            Assert.notNull(text, "text cannot be null");
            return this.user(text, Charset.defaultCharset());
        }

        public ChatClientRequestSpec user(Consumer<PromptUserSpec> consumer) {
            Assert.notNull(consumer, "consumer cannot be null");

            var us = new CustomChatClient.DefaultPromptUserSpec();
            consumer.accept(us);
            this.userText = StringUtils.hasText(us.text()) ? us.text() : this.userText;
            this.userParams.putAll(us.params());
            this.media.addAll(us.media());
            return this;
        }

        public CallResponseSpec call() {
            return new CustomChatClient.DefaultCallResponseSpec(this);
        }

        public StreamResponseSpec stream() {
            return new CustomChatClient.DefaultStreamResponseSpec(this);
        }

    }

    // Prompt

    public static class DefaultCallPromptResponseSpec implements CallPromptResponseSpec {

        private final ChatModel chatModel;

        private final Prompt prompt;

        public DefaultCallPromptResponseSpec(ChatModel chatModel, Prompt prompt) {
            Assert.notNull(chatModel, "chatModel cannot be null");
            Assert.notNull(prompt, "prompt cannot be null");
            this.chatModel = chatModel;
            this.prompt = prompt;
        }

        public String content() {
            return doGetChatResponse(this.prompt).getResult().getOutput().getContent();
        }

        public List<String> contents() {
            return doGetChatResponse(this.prompt).getResults().stream().map(r -> r.getOutput().getContent()).toList();
        }

        public ChatResponse chatResponse() {
            return doGetChatResponse(this.prompt);
        }

        private ChatResponse doGetChatResponse(Prompt prompt) {
            return this.chatModel.call(prompt);
        }

    }

    public static class DefaultStreamPromptResponseSpec implements StreamPromptResponseSpec {

        private final Prompt prompt;

        private final StreamingChatModel chatModel;

        public DefaultStreamPromptResponseSpec(StreamingChatModel streamingChatModel, Prompt prompt) {
            Assert.notNull(streamingChatModel, "streamingChatModel cannot be null");
            Assert.notNull(prompt, "prompt cannot be null");
            this.chatModel = streamingChatModel;
            this.prompt = prompt;
        }

        public Flux<ChatResponse> chatResponse() {
            return doGetFluxChatResponse(this.prompt);
        }

        private Flux<ChatResponse> doGetFluxChatResponse(Prompt prompt) {
            return this.chatModel.stream(prompt);
        }

        public Flux<String> content() {
            return doGetFluxChatResponse(this.prompt).map(r -> {
                if (r.getResult() == null || r.getResult().getOutput() == null
                        || r.getResult().getOutput().getContent() == null) {
                    return "";
                }
                return r.getResult().getOutput().getContent();
            }).filter(StringUtils::hasLength);
        }

    }


}
