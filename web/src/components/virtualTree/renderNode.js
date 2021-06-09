export default {
  name: 'NodeRender',
  functional: true,
  props: {
    render: Function,
    item: Object,
  },
  render: (h, ctx) => {
    const params = {
      item: ctx.props.item,
    };
    return ctx.props.render(h, params);
  }
};