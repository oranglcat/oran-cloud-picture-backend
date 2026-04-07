package com.oran.oranpicturebackend.manager;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.oran.oranpicturebackend.common.ErrorCode;
import com.oran.oranpicturebackend.config.QwenConfig;
import com.oran.oranpicturebackend.exception.BusinessException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class QwenAiManager {

    @Resource
    private QwenConfig qwenConfig;

    private MultiModalConversation conv;

    public MultiModalConversation getConv() {
        if (conv == null) {
            conv = new MultiModalConversation();
        }
        return conv;
    }

    /**
     * 使用通义千问VL审核图片
     * @param imageUrl 图片URL
     * @return 审核结果
     */
    public AiReviewResult reviewImage(String imageUrl) {
        if (StrUtil.isBlank(qwenConfig.getApiKey())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI API Key未配置");
        }
        
        if (StrUtil.isBlank(imageUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL不能为空");
        }

        try {
            String prompt = buildReviewPrompt();
            MultiModalConversationResult result = callQwenVL(imageUrl, prompt);
            
            return parseReviewResult(result);
            
        } catch (BusinessException e) {
            throw e;
        } catch (NoApiKeyException e) {
            log.error("API Key无效", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI服务认证失败");
        } catch (ApiException | InputRequiredException e) {
            log.error("调用通义千问VL失败, imageUrl: {}", imageUrl, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI审核服务异常: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI审核处理失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI审核失败");
        }
    }

    /**
     * 构建审核提示词
     */
    private String buildReviewPrompt() {
        return "你是一名专业的图片内容审核员。请仔细分析这张图片，判断是否适合在公开平台展示。\n\n" +
               "请检查以下内容：\n" +
               "1. 是否包含色情、裸露、性暗示内容\n" +
               "2. 是否包含暴力、血腥、恐怖内容\n" +
               "3. 是否包含政治敏感、违法违规内容\n" +
               "4. 是否包含广告、二维码、水印等营销内容\n" +
               "5. 图片质量是否过低（模糊、失真）\n\n" +
               "请以严格的JSON格式返回审核结果（不要添加任何其他文字）：\n" +
               "{\n" +
               "  \"pass\": true或false,\n" +
               "  \"score\": 0到100的整数（分数越高越安全）,\n" +
               "  \"reason\": \"简短说明审核原因\",\n" +
               "  \"violations\": [\"违规类型1\", \"违规类型2\"],\n" +
               "  \"tags\": [\"标签1\", \"标签2\"]\n" +
               "}";
    }

    /**
     * 调用通义千问VL模型
     */
    private MultiModalConversationResult callQwenVL(String imageUrl, String prompt)
            throws ApiException, NoApiKeyException, InputRequiredException, UploadFileException {
        
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("image", imageUrl),
                        Collections.singletonMap("text", prompt)
                ))
                .build();

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(qwenConfig.getApiKey())
                .model(qwenConfig.getModel())
                .messages(Collections.singletonList(userMessage))
                .temperature(qwenConfig.getTemperature())
                .build();

        return getConv().call(param);
    }

    /**
     * 解析审核结果
     */
    private AiReviewResult parseReviewResult(MultiModalConversationResult result) {
        try {
            String content = result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString();
            
            log.info("通义千问VL原始响应: {}", content);
            
            AiReviewResult reviewResult = JSONUtil.toBean(content, AiReviewResult.class);
            
            if (reviewResult == null || reviewResult.getScore() == null) {
                return createFallbackResult(false, "AI响应解析失败");
            }
            
            reviewResult.setRawResponse(content);
            
            if (reviewResult.getViolations() == null) {
                reviewResult.setViolations(new ArrayList<>());
            }
            if (reviewResult.getTags() == null) {
                reviewResult.setTags(new ArrayList<>());
            }
            
            return reviewResult;
            
        } catch (Exception e) {
            log.error("解析AI审核结果失败", e);
            return createFallbackResult(false, "审核结果解析失败");
        }
    }


    /**
     * 创建降级结果
     */
    private AiReviewResult createFallbackResult(boolean pass, String reason) {
        AiReviewResult result = new AiReviewResult();
        result.setPass(pass);
        result.setScore(pass ? 80 : 20);
        result.setReason(reason);
        result.setViolations(new ArrayList<>());
        result.setTags(new ArrayList<>());
        result.setRawResponse("fallback");
        return result;
    }

    /**
     * AI审核结果
     */
    @Data
    public static class AiReviewResult {
        private Boolean pass;
        private Integer score;
        private String reason;
        private List<String> violations;
        private List<String> tags;
        private String rawResponse;
    }
}
