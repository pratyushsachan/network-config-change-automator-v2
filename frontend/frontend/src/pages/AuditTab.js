import React from 'react';
import { motion } from 'framer-motion';
import { User, Clock, FileText } from 'lucide-react';
import { mockAudit } from '../mockData';

export default function AuditTab() {
  const getActionColor = (action) => {
    switch (action) {
      case 'CREATED':
        return 'from-blue-500/20 to-cyan-500/20 border-blue-400/30';
      case 'APPROVED':
        return 'from-green-500/20 to-emerald-500/20 border-green-400/30';
      case 'DEPLOYED':
        return 'from-purple-500/20 to-pink-500/20 border-purple-400/30';
      default:
        return 'from-gray-500/20 to-slate-500/20 border-gray-400/30';
    }
  };

  return (
    <div className="space-y-6">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="bg-white/10 backdrop-blur-md rounded-2xl border border-white/20 p-6"
      >
        <h2 className="text-2xl font-bold text-white mb-2">Audit Log</h2>
        <p className="text-purple-200">Complete history of all changes and actions</p>
      </motion.div>

      <div className="space-y-3">
        {mockAudit.map((event, idx) => (
          <motion.div
            key={event.id}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.1 + idx * 0.1 }}
            whileHover={{ scale: 1.02 }}
            className={`bg-gradient-to-r ${getActionColor(event.action)} backdrop-blur-md rounded-xl border p-4 hover:border-white/40 transition-all`}
          >
            <div className="flex items-start gap-4">
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 3, repeat: Infinity, ease: 'linear' }}
                className="mt-1"
              >
                <Clock size={20} className="text-purple-300" />
              </motion.div>
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span className="px-3 py-1 rounded-full bg-white/20 text-white text-xs font-bold">
                    {event.action}
                  </span>
                  <span className="text-purple-300 text-sm">
                    Change #{event.changeId}
                  </span>
                </div>
                <p className="text-white font-semibold mb-2">{event.detail}</p>
                <div className="flex items-center gap-3 text-sm text-purple-200">
                  <span className="flex items-center gap-1">
                    <User size={16} />
                    {event.actor}
                  </span>
                  <span className="flex items-center gap-1">
                    <Clock size={16} />
                    {new Date(event.timestamp).toLocaleString()}
                  </span>
                </div>
              </div>
              <FileText size={20} className="text-purple-400" />
            </div>
          </motion.div>
        ))}
      </div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
        className="bg-purple-500/20 backdrop-blur-md rounded-xl border border-purple-400/30 p-4 text-center"
      >
        <p className="text-white font-semibold">Total Events: {mockAudit.length}</p>
        <p className="text-purple-200 text-sm">All actions are logged with timestamps and actor information</p>
      </motion.div>
    </div>
  );
}
