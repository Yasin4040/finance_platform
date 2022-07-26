package com.jtyjy.finance.manager.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.List;

/**
 * @author User
 */
@Data
public class TreeNode<T> {

    protected Long id;
    protected Long parentId;
    protected Boolean leaf;

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    protected List<T> children;

}
