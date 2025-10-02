package com.challenge.usecase;

import com.challenge.model.Comment;
import java.util.List;

@FunctionalInterface
public interface GetCommentsByPost { List<Comment> execute(Integer postId); }

