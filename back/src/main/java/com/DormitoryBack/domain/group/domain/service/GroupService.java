package com.DormitoryBack.domain.group.domain.service;

import com.DormitoryBack.domain.article.comment.domain.entity.Comment;
import com.DormitoryBack.domain.article.domain.entity.Article;
import com.DormitoryBack.domain.article.domain.repository.ArticleRepository;
import com.DormitoryBack.domain.group.domain.dto.request.GroupCreateDto;
import com.DormitoryBack.domain.group.domain.dto.response.GroupChangedDto;
import com.DormitoryBack.domain.group.domain.dto.response.GroupCreatedDto;
import com.DormitoryBack.domain.group.domain.dto.response.GroupListDto;
import com.DormitoryBack.domain.group.domain.entitiy.Group;
import com.DormitoryBack.domain.group.domain.repository.GroupRepository;
import com.DormitoryBack.domain.jwt.TokenProvider;
import com.DormitoryBack.domain.member.dto.UserResponseDTO;
import com.DormitoryBack.domain.member.entity.User;
import com.DormitoryBack.domain.member.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private final RedisTemplate<String,Long> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public TokenProvider tokenProvider;

    public GroupService(RedisTemplate<String,Long> redisTemplate){
        this.redisTemplate=redisTemplate;
    }

    public GroupCreatedDto createNewGroup(GroupCreateDto requestDto) {
        SetOperations<String,Long> setOperations=redisTemplate.opsForSet();
        Long articleId=requestDto.getArticleId();
        if(requestDto.getMaxCapacity()==null){
            requestDto.setMaxCapacity(4L);
        }
        else if(requestDto.getMaxCapacity()<=1L ){
            throw new RuntimeException("MaxCapacityMustNotLessThan2");
        }
        else if(requestDto.getMaxCapacity()>=100L){
            throw new RuntimeException("MaxCapacityCannotExceed99");
        }
        Article article=articleRepository.findById(articleId).orElse(null);
        if(article==null){
            throw new RuntimeException("ArticleNotFound");
        }
        Long hostId=article.getUserId();
        Long NotBelongToAnywhere=setOperations.add("groupGlobal",hostId);
        if(NotBelongToAnywhere==0L){
            throw new RuntimeException("DuplicatedParticipation");
        }

        Group newGroup=Group.builder()
                .dormId(article.getDorId())
                .hostId(hostId)
                .maxCapacity(requestDto.getMaxCapacity())
                .article(article)
                .createdTime(LocalDateTime.now())
                .category(article.getCategory())
                .isProceeding(true)
                .build();

        Group saved=groupRepository.save(newGroup);
        GroupCreatedDto responseDto=GroupCreatedDto.builder()
                .id(saved.getId())
                .dormId(saved.getDormId())
                .hostId(saved.getHostId())
                .category(saved.getCategory())
                .maxCapacity(saved.getMaxCapacity())
                .articleId(saved.getArticleId())
                .build();

        setOperations.add(String.valueOf(newGroup.getId()),newGroup.getHostId());

        return responseDto;
    }

    public GroupListDto getAllGroups() {
        List<Group> groups=groupRepository.findAll();

        List<GroupCreatedDto> createdDtoList=this
                .groupListToCreatedDtoList(groups);
        Long groupCnt=Long.valueOf(groups.size());
        List<String> stringified=stringifyDtoList(createdDtoList);
        GroupListDto responseDto=GroupListDto.builder()
                .groups(stringified)
                .numberOfGroup(groupCnt)
                .build();

        return responseDto;
    }

    public GroupListDto getAllProceedingGroups() {
        List<Group> groups=groupRepository.findAllByIsProceeding(true);
        Long groupCnt=Long.valueOf(groups.size());
        List<GroupCreatedDto> createdDtoList=this
                .groupListToCreatedDtoList(groups);
        List<String> stringified=stringifyDtoList(createdDtoList);
        GroupListDto responseDto=GroupListDto.builder()
                .groups(stringified)
                .numberOfGroup(groupCnt)
                .build();

        return responseDto;
    }

    public List<GroupCreatedDto> groupListToCreatedDtoList(List<Group> groups){
        SetOperations<String,Long> setOperations=redisTemplate.opsForSet();
        List<GroupCreatedDto> createdDtoList=new ArrayList<>();
        Iterator<Group> iterator=groups.iterator();

        while(iterator.hasNext()){
            Group group=iterator.next();
            Long numMembers=setOperations.size(String.valueOf(group.getId()));
            GroupCreatedDto responseDto=GroupCreatedDto.builder()
                    .id(group.getId())
                    .dormId(group.getDormId())
                    .hostId(group.getHostId())
                    .articleId(group.getArticleId())
                    .category(group.getCategory())
                    .maxCapacity(group.getMaxCapacity())
                    .isProceeding(group.getIsProceeding())
                    .createdTime(group.getCreatedTime())
                    .currentNumberOfMembers(numMembers)
                    .build();

            createdDtoList.add(responseDto);
        }
        return createdDtoList;
    }


    public long getNumberOfMembers(Long groupId) {
        if(groupId==-1L){
            throw new RuntimeException("GroupIdNotGiven");
        }
        long num=redisTemplate.opsForSet().size(groupId.toString());
        if(num==0){
            throw new RuntimeException("GroupNotFound");
        }
        return num;
    }

    public GroupChangedDto participateInGroup(Long groupId, String token) {
        SetOperations<String,Long> setOperations=redisTemplate.opsForSet();
        if(!tokenProvider.validateToken(token)){
            throw new JwtException("InvalidToken");
        }
        if(groupId==-1L){
            throw new RuntimeException("GroupIdNotGiven");
        }
        Long userId=tokenProvider.getUserIdFromToken(token);
        Long numBeforeAdding=setOperations.size(String.valueOf(groupId));
        Group targetGroup=groupRepository.findById(groupId).orElse(null);
        Boolean isMemberOfTargetGroup=setOperations
                .isMember(String.valueOf(targetGroup.getId()),userId);
        if(isMemberOfTargetGroup==true){
            throw new RuntimeException("AlreadyBelongToThisGroup");
        }
        Long notBelongToAnywhere=setOperations.add("groupGlobal",userId);
        if(notBelongToAnywhere==0L){
            throw new RuntimeException("DuplicatedParticipation");
        }
        if(numBeforeAdding==targetGroup.getMaxCapacity()){
            throw new RuntimeException("GroupFull");
        }
        setOperations.add(String.valueOf(targetGroup.getId()),userId);
        User user=userRepository.findById(userId).orElse(null);
        UserResponseDTO userChanges= UserResponseDTO.builder()
                .eMail(user.getEMail())
                .nickName(user.getNickName())
                .build();

        GroupChangedDto responseDto=GroupChangedDto.builder()
                .mode("join")
                .userChanges(userChanges)
                .numberOfRemainings(numBeforeAdding+1L)
                .build();

        return responseDto;
    }

    public GroupChangedDto leaveGroup(Long groupId, String token){
        SetOperations<String,Long> setOperations=redisTemplate.opsForSet();
        if(!tokenProvider.validateToken(token)){
            throw new JwtException("InvalidToken");
        }
        if(groupId==-1L){
            throw new RuntimeException("GroupIdNotGiven");
        }
        Long userId=tokenProvider.getUserIdFromToken(token);
        Long numBeforeAdding=setOperations.size(String.valueOf(groupId));
        Group targetGroup=groupRepository.findById(groupId).orElse(null);
        Boolean isMemberOfTargetGroup=setOperations
                .isMember(String.valueOf(targetGroup.getId()),userId);

        if(isMemberOfTargetGroup==false){
            throw new RuntimeException("UserNotBelongToGroupToLeave");
        }
        else if(userId==targetGroup.getHostId()){
            throw new RuntimeException("HostCannotLeaveGroup");
        }
        setOperations.remove(String.valueOf(targetGroup.getId()),userId);
        setOperations.remove("groupGlobal",userId);
        User user=userRepository.findById(userId).orElse(null);
        UserResponseDTO userChanges=UserResponseDTO.builder()
                .eMail(user.getEMail())
                .nickName(user.getNickName())
                .build();

        GroupChangedDto responseDto=GroupChangedDto.builder()
                .mode("quit")
                .userChanges(userChanges)
                .numberOfRemainings(numBeforeAdding-1L)
                .build();

        return responseDto;
    }

    public List<String> stringifyDtoList(List<GroupCreatedDto> dtoList){
        List<String> stringifiedDtoList=dtoList.stream()
                .map(GroupCreatedDto::toJsonString)
                .collect(Collectors.toList());

        return stringifiedDtoList;
    }



}
