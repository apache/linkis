import { db } from '@/common/service/db/index.js';

export default {
  name: 'dssIndexedDB',
  methods: {
    deleteDb() {
      db.db.delete();
    },
  }
}