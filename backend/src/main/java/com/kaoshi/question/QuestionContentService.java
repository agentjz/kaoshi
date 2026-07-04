package com.kaoshi.question;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.question.dto.QuestionAttachmentRequest;
import com.kaoshi.question.dto.QuestionAttachmentResponse;
import com.kaoshi.question.dto.QuestionContentNodeResponse;
import com.kaoshi.question.dto.QuestionContentTreeResponse;
import com.kaoshi.question.dto.QuestionNodeOptionRequest;
import com.kaoshi.question.dto.QuestionNodeOptionResponse;
import com.kaoshi.question.dto.QuestionNodeSaveRequest;
import com.kaoshi.question.domain.QuestionNode;
import com.kaoshi.question.mapper.QuestionContentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class QuestionContentService {
    private static final Set<String> NODE_TYPES = Set.of("SECTION", "GROUP");

    private final QuestionContentMapper contentMapper;
    private final QuestionResponseAssembler responseAssembler;

    public QuestionContentService(QuestionContentMapper contentMapper, QuestionResponseAssembler responseAssembler) {
        this.contentMapper = contentMapper;
        this.responseAssembler = responseAssembler;
    }

    public QuestionContentTreeResponse tree(Long bankId) {
        ensureBankExists(bankId);
        Map<Long, NodeDraft> drafts = new LinkedHashMap<>();
        for (QuestionNode node : contentMapper.findNodesByBank(bankId)) {
            drafts.put(node.getId(), new NodeDraft(node, List.of(), List.of()));
        }
        for (NodeDraft draft : drafts.values()) {
            Long parentId = draft.parentId();
            if (parentId != null && drafts.containsKey(parentId)) {
                drafts.get(parentId).children().add(draft);
            }
        }
        List<QuestionContentNodeResponse> sections = drafts.values().stream()
                .filter(draft -> draft.parentId() == null)
                .map(this::toResponse)
                .toList();
        return new QuestionContentTreeResponse(
                bankId,
                contentMapper.findBankName(bankId),
                sections,
                contentMapper.findUngroupedQuestions(bankId).stream()
                        .map(responseAssembler::toResponse)
                        .toList()
        );
    }

    @Transactional
    public QuestionContentNodeResponse createNode(Long bankId, QuestionNodeSaveRequest request) {
        ensureBankExists(bankId);
        validateNodeSave(bankId, null, request);
        QuestionNode node = node(null, bankId, request);
        contentMapper.insertNode(node);
        Long nodeId = node.getId();
        replaceNodeOptions(nodeId, request.sharedOptions());
        replaceNodeAttachments(nodeId, request.attachments());
        return nodeDetail(nodeId);
    }

    @Transactional
    public QuestionContentNodeResponse updateNode(Long nodeId, QuestionNodeSaveRequest request) {
        QuestionNode existing = findNode(nodeId);
        Long bankId = existing.getBankId();
        validateNodeSave(bankId, nodeId, request);
        QuestionNode node = node(nodeId, bankId, request);
        contentMapper.updateNode(node);
        replaceNodeOptions(nodeId, request.sharedOptions());
        replaceNodeAttachments(nodeId, request.attachments());
        return nodeDetail(nodeId);
    }

    @Transactional
    public void deleteNode(Long nodeId) {
        findNode(nodeId);
        if (contentMapper.countChildNodes(nodeId) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "节点下存在子节点，不能删除");
        }
        if (contentMapper.countQuestionsByNode(nodeId) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "节点下存在小题，不能删除");
        }
        contentMapper.deleteNodeOptions(nodeId);
        contentMapper.deleteNodeAttachments(nodeId);
        contentMapper.deleteNode(nodeId);
    }

    public QuestionContentNodeResponse nodeDetail(Long nodeId) {
        QuestionNode node = findNode(nodeId);
        return toResponse(new NodeDraft(node, nodeOptions(nodeId), nodeAttachments(nodeId)));
    }

    private void validateNodeSave(Long bankId, Long nodeId, QuestionNodeSaveRequest request) {
        if (!NODE_TYPES.contains(request.nodeType())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "节点类型不合法");
        }
        if ("SECTION".equals(request.nodeType()) && request.parentId() != null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "大组不能挂在其它节点下");
        }
        if ("GROUP".equals(request.nodeType()) && request.parentId() != null) {
            QuestionNode parent = findNode(request.parentId());
            if (!bankId.equals(parent.getBankId()) || !"SECTION".equals(parent.getNodeType())) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "小组只能挂在当前题库的大组下");
            }
        }
        int duplicate = nodeId == null
                ? contentMapper.countNodeCode(bankId, request.nodeCode())
                : contentMapper.countNodeCodeExceptId(bankId, request.nodeCode(), nodeId);
        if (duplicate > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "节点编码已存在");
        }
        validateSharedOptions(request.sharedOptions());
    }

    private void validateSharedOptions(List<QuestionNodeOptionRequest> options) {
        Set<String> labels = new HashSet<>();
        for (QuestionNodeOptionRequest option : options == null ? List.<QuestionNodeOptionRequest>of() : options) {
            String label = option.label().trim().toUpperCase();
            if (!labels.add(label)) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "共享选项标签不能重复");
            }
        }
    }

    private void ensureBankExists(Long bankId) {
        if (contentMapper.countBankById(bankId) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "题库不存在");
        }
    }

    private QuestionNode findNode(Long nodeId) {
        QuestionNode node = contentMapper.findNodeById(nodeId);
        if (node == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "内容节点不存在");
        }
        return node;
    }

    private QuestionNode node(Long nodeId, Long bankId, QuestionNodeSaveRequest request) {
        QuestionNode node = new QuestionNode();
        node.setId(nodeId);
        node.setBankId(bankId);
        node.setParentId(request.parentId());
        node.setNodeCode(request.nodeCode().trim());
        node.setNodeType(request.nodeType());
        node.setTitle(request.title());
        node.setDirection(request.direction());
        node.setMaterial(request.material());
        node.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        return node;
    }

    private void replaceNodeOptions(Long nodeId, List<QuestionNodeOptionRequest> options) {
        contentMapper.deleteNodeOptions(nodeId);
        int sort = 10;
        for (QuestionNodeOptionRequest option : options == null ? List.<QuestionNodeOptionRequest>of() : options) {
            contentMapper.insertNodeOption(nodeId, option.label().trim().toUpperCase(), option.content(), sort);
            sort += 10;
        }
    }

    private void replaceNodeAttachments(Long nodeId, List<QuestionAttachmentRequest> attachments) {
        contentMapper.deleteNodeAttachments(nodeId);
        int sort = 10;
        for (QuestionAttachmentRequest attachment : attachments == null ? List.<QuestionAttachmentRequest>of() : attachments) {
            contentMapper.insertNodeAttachment(nodeId, attachment.fileName(), attachment.fileUrl(), attachment.mediaType(), sort);
            sort += 10;
        }
    }

    private List<QuestionNodeOptionResponse> nodeOptions(Long nodeId) {
        return contentMapper.findNodeOptions(nodeId).stream()
                .map(option -> new QuestionNodeOptionResponse(
                        option.getId(),
                        option.getOptionLabel(),
                        option.getContent(),
                        option.getSortOrder()
                ))
                .toList();
    }

    private List<QuestionAttachmentResponse> nodeAttachments(Long nodeId) {
        return contentMapper.findNodeAttachments(nodeId).stream()
                .map(attachment -> new QuestionAttachmentResponse(
                        attachment.getId(),
                        attachment.getFileName(),
                        attachment.getFileUrl(),
                        attachment.getMediaType(),
                        attachment.getSortOrder()
                ))
                .toList();
    }

    private QuestionContentNodeResponse toResponse(NodeDraft draft) {
        Long id = draft.id();
        return new QuestionContentNodeResponse(
                id,
                draft.parentId(),
                draft.node().getNodeCode(),
                draft.node().getNodeType(),
                draft.node().getTitle(),
                draft.node().getDirection(),
                draft.node().getMaterial(),
                draft.node().getSortOrder(),
                draft.options().isEmpty() ? nodeOptions(id) : draft.options(),
                draft.attachments().isEmpty() ? nodeAttachments(id) : draft.attachments(),
                contentMapper.findQuestionsByNode(id).stream()
                        .map(responseAssembler::toResponse)
                        .toList(),
                draft.children().stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    private record NodeDraft(
            QuestionNode node,
            List<QuestionNodeOptionResponse> options,
            List<QuestionAttachmentResponse> attachments,
            List<NodeDraft> children
    ) {
        NodeDraft(QuestionNode node, List<QuestionNodeOptionResponse> options, List<QuestionAttachmentResponse> attachments) {
            this(node, options, attachments, new ArrayList<>());
        }

        Long id() {
            return node.getId();
        }

        Long parentId() {
            return node.getParentId();
        }
    }
}
