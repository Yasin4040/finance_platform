package com.jtyjy.finance.manager.utils;

import com.jtyjy.finance.manager.vo.TreeNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author User
 */
public class TreeUtil {

    /**
     * 使用递归方法建树
     *
     * @param treeNodes 构建树集合
     * @param <T>       泛型
     * @return 结果集
     */
    public static <T extends TreeNode<T>> List<T> build(List<T> treeNodes) {
        return build(treeNodes, 0L);
    }

    /**
     * 使用递归方法建树
     *
     * @param treeNodes 构建树集合
     * @param root      顶级父Id
     * @param <T>       泛型
     * @return 结果集
     */
    public static <T extends TreeNode<T>> List<T> build(List<T> treeNodes, Long root) {
        List<T> trees = new ArrayList<>();
        for (T treeNode : treeNodes) {
            if (root.equals(treeNode.getParentId())) {
                trees.add(findChildren(treeNode, treeNodes));
            }
        }
        return trees;
    }

    /**
     * 递归查找子节点
     */
    private static <T extends TreeNode<T>> T findChildren(T treeNode, List<T> treeNodes) {
        for (T it : treeNodes) {
            if (treeNode.getId().equals(it.getParentId())) {
                if (treeNode.getChildren() == null) {
                    treeNode.setChildren(new ArrayList<>());
                }
                treeNode.getChildren().add(findChildren(it, treeNodes));
            }
        }
        return treeNode;
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * 判断树结构是否叶子节点(构建树结构之前)
     */
    public static <T extends TreeNode<T>> void judgeLeafBefore(List<T> list) {
        Set<Long> parentIds = list.stream().map(TreeNode::getParentId).collect(Collectors.toSet());
        list.forEach(v -> v.setLeaf(!parentIds.contains(v.getId())));
    }

    /**
     * 判断树结构是否叶子节点(构建树结构之后)
     */
    public static <T extends TreeNode<T>> void judgeLeafAfter(List<T> treeList) {
        for (T node : treeList) {
            if (node.getChildren() == null || node.getChildren().isEmpty()) {
                node.setLeaf(true);
            } else {
                node.setLeaf(false);

                judgeLeafAfter(node.getChildren());
            }
        }
    }

    /**
     * 排除无子节点的父节点(该方法必须保证 leaf值 不为空)
     */
    public static <T extends TreeNode<T>> boolean excludeParentNode(List<T> treeList) {
        if (treeList == null || treeList.isEmpty()) {
            return true;
        }
        boolean flag = true;
        Iterator<T> iterator = treeList.iterator();
        while (iterator.hasNext()) {
            T node = iterator.next();
            if (!node.getLeaf()) {
                if (excludeParentNode(node.getChildren())) {
                    iterator.remove();
                } else {
                    // 当前父级节点下，存在叶子节点，不删除该父级
                    flag = false;
                }
            } else {
                // 只要有叶子节点，不删除该父级
                flag = false;
            }
        }
        // 是否删除该父级
        return flag;
    }
}
