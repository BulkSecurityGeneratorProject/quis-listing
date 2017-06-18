package com.manev.quislisting.service.post;

import com.manev.quislisting.domain.User;
import com.manev.quislisting.domain.post.discriminator.Attachment;
import com.manev.quislisting.repository.UserRepository;
import com.manev.quislisting.repository.post.PostRepository;
import com.manev.quislisting.security.SecurityUtils;
import com.manev.quislisting.service.post.dto.AttachmentDTO;
import com.manev.quislisting.service.post.mapper.AttachmentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AttachmentService {

    private final Logger log = LoggerFactory.getLogger(AttachmentService.class);
    private PostRepository<Attachment> postRepository;
    private AttachmentMapper attachmentMapper;
    private UserRepository userRepository;

    public AttachmentService(PostRepository<Attachment> postRepository, AttachmentMapper attachmentMapper,
                             UserRepository userRepository) {
        this.postRepository = postRepository;
        this.attachmentMapper = attachmentMapper;
        this.userRepository = userRepository;
    }

    public Page<AttachmentDTO> findAll(Pageable pageable) {
        log.debug("Request to get all AttachmentDTOs");
        Page<Attachment> result = postRepository.findAll(pageable);
        return result.map(attachmentMapper::attachmentToAttachmentDTO);
    }

    @Transactional(readOnly = true)
    public AttachmentDTO findOne(Long id) {
        log.debug("Request to get AttachmentDTO: {}", id);
        Attachment result = postRepository.findOne(id);
        return result != null ? attachmentMapper.attachmentToAttachmentDTO(result) : null;
    }

    public void delete(Long id) {
        log.debug("Request to delete AttachmentDTO : {}", id);
        postRepository.delete(id);
    }

    public AttachmentDTO saveAttachmentAsTemp(AttachmentDTO attachmentDTO) {
        Attachment attachment = getAttachment(attachmentDTO);
        attachment.setStatus(Attachment.Status.TEMP);
        return saveAttachment(attachment, SecurityUtils.getCurrentUserLogin());
    }

    public AttachmentDTO saveAttachmentByAdmin(AttachmentDTO attachmentDTO) {
        Attachment attachment = getAttachment(attachmentDTO);
        attachment.setStatus(Attachment.Status.BY_ADMIN);
        return saveAttachment(attachment, SecurityUtils.getCurrentUserLogin());
    }

    private AttachmentDTO saveAttachment(Attachment attachment, String currentUserLogin) {
        Optional<User> oneByLogin = userRepository.findOneByLogin(currentUserLogin);
        if (oneByLogin.isPresent()) {
            attachment.setUser(oneByLogin.get());
            Attachment attachmentSaved = postRepository.save(attachment);
            return attachmentMapper.attachmentToAttachmentDTO(attachmentSaved);
        } else {
            throw new UsernameNotFoundException("User " + currentUserLogin + " was not found in the " +
                    "database");
        }
    }

    private Attachment getAttachment(AttachmentDTO attachmentDTO) {
        Attachment attachment = attachmentMapper.attachmentDTOToAttachment(attachmentDTO);
        attachment.setCreated(attachmentDTO.getCreated());
        attachment.setModified(attachmentDTO.getModified());
        return attachment;
    }

    public AttachmentDTO save(AttachmentDTO attachmentDTO) {
        return null;
    }
}
