
<template>
  <div
    v-show="node.visible"
    :class="nodeClasses"
    :fsType="fsType"
    class="we-tree-node"
  >
    <div
      :title="node.data.path"
      :style="contentStyle"
      class="we-tree-node__content"
      @contextmenu.prevent.stop="handleContextMenu"
      @click.stop="handleClick"
      @keyup.stop="handleKeyUp"
      @dblclick.stop="handleDblClick">
      <span v-if="expanded" class="fi-folder-o"/>
      <span v-else :style="'color: ' + supportIcon(node).color" :class="[ supportIcon(node).className, supportIcon(node).icon ]" />
      <template v-if="node.isEditState">
        <div
          :class="{'is-error': !inputValid}"
          class="we-tree-node__edit">
          <input
            ref="edit"
            :value="node.label"
            type="text"
            @click.stop
            @keyup.stop
            @blur="endNodeEdit"
            @keyup.enter.stop.prevent="endNodeEdit">
          <div
            v-if="errorMsg"
            class="we-tree-node__error">{{ errorMsg }}</div>
        </div>
      </template>
      <template v-else-if="node.data && node.data.isFn">
        <div class="we-tree-node__checkbox">
          <input
            v-model="node.data.load"
            :value="node.data.name"
            :disabled="node.data.disabled || (node.data.type==='self' && node.data.shared)"
            type="checkbox"
            @change="hanlderCheck">
          <view-modal :node="node.data">
            <span
              :title="node.data.name"
              :class="{expired: node.data.expire}">{{ node.data.name }}</span>
          </view-modal>
        </div>
      </template>
      <template v-else>
        <span class="we-tree-node__label">{{ node.label }}</span>
      </template>
    </div>
    <div
      v-show="expanded"
      class="we-tree-node__children">
      <we-tree-node
        v-for="(child, index) in node.computedNodes"
        :key="getNodeKey(child, index)"
        :node="child"
        :fs-type="fsType"
        :highlight-path="highlightPath"
        :currentNode="currentNode"/>
    </div>
  </div>
</template>
<script>
import mixin from './mixin.js';
import viewModal from './functionView';
import SUPPORTED_LANG_MODES from '@/common/config/scriptis.js';
export default {
  name: 'WeTreeNode',
  components: {
    viewModal,
  },
  mixins: [mixin],
  props: {
    node: {
      type: Object,
      default: () => {},
    },
    currentNode: {
      type: Object,
      defalut: {}
    }
  },
  data() {
    return {
      tree: null,
      expanded: false,
      inputValid: true,
      errorMsg: null,
      nameList: [],
      SUPPORTED_LANG_MODES
    };
  },
  computed: {
    isHasFileOpen() {
      if (!this.highlightPath) return false;
      const highlightList = this.highlightPath.split('/');
      // 目录一一对应,需递归匹配所有父层级
      let flag = false;
      const checkName = (node) => {
        const name = node.data.name;
        const needToHighLighted = highlightList[node._level - 1];
        if (name === needToHighLighted) {
          if (node.parent) {
            checkName(node.parent);
          } else {
            flag = true;
          }
        }
      };
      checkName(this.node);
      return flag;
    },
    nodeClasses() {
      return {
        'is-expanded': this.expanded,
        'is-current': this.tree.store.currentNode === this.node,
        'is-actived': this.isHasFileOpen,
      };
    },
    contentStyle() {
      return {
        'padding-left': this.node._level * this.tree.indent + 'px',
      };
    },
  },
  watch: {
    'node.isEditState'(val) {
      if (val) {
        this.$nextTick(() => {
          this.$refs.edit && this.$refs.edit.focus();
          // 设置选中区域
          // index为-1时，是文件夹，会默认选中全部
          if (this.node.label) {
            const index = this.node.label.lastIndexOf('.');
            this.$refs.edit.setSelectionRange(0, index);
          }
        });
      }
    },
    'node.data.expanded'(val) {
      this.expanded = val;
    },
    'currentNode.isEditState'(val) {
      if (this.currentNode.data.path === this.node.data.path && val) {
        this.node.changeEditState(val);
      }
    }
  },
  created() {
    const parent = this.$parent;
    this.tree = parent.isTree ? parent : parent.tree;
    this.initData();
    this.initWatch();
  },
  methods: {
    initData() {
      this.node.isDirNull = true;
      let treeData = null;
      treeData = this.tree.data[0].children;
      if (treeData) {
        treeData.forEach((e) => {
          if (e.isLeaf) {
            this.nameList.push(e.name); // todo
          }
        });
      }
      // 初始化的时候默认打开根目录
      if (this.isRootDefaultOpen) {
        this.expanded = true;
      }
    },
    initWatch() {
      const nodeProps = this.tree.nodeProps || {};
      const propName = nodeProps.children || 'children';
      this.$watch(`node.data.${propName}`, () => {
        this.node.updateChildren();
      });
    },
    handleContextMenu(e) {
      const tree = this.tree;
      const store = tree.store;
      store.setCurrentNode(this.node);
      this.nameList = [];
      const nodeData = this.node.data;
      const childrenKey = tree.nodeProps.children || 'children';
      const that = this;
      if (nodeData && nodeData.children && nodeData.children.length) {
        nodeData.children.forEach((node) => {
          if (node.isLeaf) {
            this.nameList.push(node.name);
          }
        });
      } else if (tree && tree.loadDataFn && !nodeData.isLeaf) {
        tree.loadDataFn(this.node, (data) => {
          if (data) {
            that.$set(that.node.data, childrenKey, data);
            tree.store.filter();
          }
        });
      }
      this.tree.$emit('node-contextmenu', e, {
        nodeData,
        node: this.node,
        nameList: this.nameList,
      });
    },
    handleClick(e) {
      const store = this.tree.store;
      store.setCurrentNode(this.node);
      if (!this.node.isLeaf) {
        if (this.expanded) {
          this.expanded = false;
        } else {
          this.handleExpand();
        }
      }
      this.tree.$emit('node-click', e, {
        nodeData: this.node.data,
        node: this.node,
      });
    },
    handleExpand() {
      const tree = this.tree;
      const childrenKey = tree.nodeProps.children || 'children';
      const that = this;
      const children = that.node.data[childrenKey] || [];
      // 如果有children数据就不去请求
      if (tree && tree.loadDataFn && !children.length) {
        tree.loadDataFn(this.node, (data) => {
          if (data) {
            that.$set(that.node.data, childrenKey, data);
            this.$nextTick(() => {
              tree.store.filter();
            });
          }
          this.expanded = true;
        });
      } else {
        that.$set(that.node.data, childrenKey, children);
        this.expanded = true;
        tree.store.filter();
      }
    },
    handleKeyUp(e) {
      this.tree.$emit('node-keyup', e, {
        nodeData: this.node.data,
        node: this.node,
      });
    },
    handleDblClick(e) {
      if (this.node.isEditState) {
        this.endNodeEdit();
      } else {
        this.tree.$emit('node-dblclick', e, {
          nodeData: this.node.data,
          node: this.node,
        });
      }
    },
    endNodeEdit() {
      if (!this.node.isEditState) return;
      const oldLabel = this.node.label;
      const newLabel = this.$refs.edit.value;
      if (oldLabel === newLabel) {
        this.node.changeEditState(false);
        this.changeInputValid(true);
        this.errorMsg = '';
        return;
      }
      if (this.tree.nodeEditValid) {
        this.errorMsg = this.tree.nodeEditValid({
          label: newLabel,
          node: this.node.data,
        });
        // 如果脚本类型是sql或者是hql是支持脚本修改后缀的，且修改的后缀是正确的
        const reg = /\.(hql|sql)$/i;
        const newResult = reg.test(newLabel);
        const oldResult = reg.test(oldLabel);
        if (oldResult) {
          if (newResult) {
            this.errorMsg = '';
          } else {
            this.errorMsg = '请填写正确后缀名';
          }
        }

        if (this.errorMsg) {
          this.node.changeEditState(true);
          this.changeInputValid(false);
          return;
        } else {
          this.node.changeEditState(false);
          this.changeInputValid(true);
          this.errorMsg = '';
        }
      }
      if (this.tree.beforeChange) {
        this.tree.beforeChange({
          label: newLabel,
          node: this.node.data,
        }, (isExist) => {
          if (!isExist) {
            this.node.changeEditState(true);
            this.changeInputValid(true);
          } else {
            this.changeInputValid(true);
            this.errorMsg = '';
            const evName = this.node.isNew ? 'node-create' : 'node-edit';
            this.node.isNew = false;
            this.node.changeEditState(false);
            this.node.label = newLabel;
            this.errorMsg = '';
            this.tree.$emit(evName, evName, {
              nodeData: this.node.data,
              node: this.node,
              oldLabel,
            });
          }
        });
      }
    },
    changeInputValid(valid) {
      this.inputValid = valid;
    },
    getNodeKey(node, index) {
      const nodeKey = this.tree.nodeKey;
      let key = index;
      if (!node.data) {
        return key;
      }
      if (nodeKey) {
        key = node.data[nodeKey];
      } else {
        key = node.data.name + index;
      }
      return key;
    },
    hanlderCheck(e) {
      this.tree.$emit('node-check', e, {
        nodeData: this.node.data,
        node: this.node,
      });
    },
    supportIcon(node) {
      const supportModes = this.SUPPORTED_LANG_MODES;
      const match = supportModes.find((item) => item.rule.test(this.node.label));
      if (node.isLeaf && match) {
        return {
          className: 'is-leaf',
          icon: match.logo,
          color: match.color
        }
      } else if (node.isLeaf && !match && !node.data.isFn) {
        return {
          className: 'is-leaf',
          icon: 'fi-file'
        }
      } else if (!node.isLeaf) {
        return {
          icon: 'fi-folder'
        }
      } else if (node.data.isFn) {
        return {
          className: 'is-checkbox',
        }
      }
    },
  },
};
</script>
