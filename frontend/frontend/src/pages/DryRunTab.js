import React from 'react';
import { motion } from 'framer-motion';
import { CheckCircle, AlertTriangle, Plus, Minus } from 'lucide-react';

export default function DryRunTab() {
  const dryRunData = [
    {
      device: 'rtr-core-01',
      os: '17.9.4',
      stage: 'CANARY',
      compatible: true,
      added: ['ntp server 10.10.0.200'],
      removed: [],
    },
    {
      device: 'rtr-core-02',
      os: '17.9.4',
      stage: 'BATCH-1',
      compatible: true,
      added: ['ntp server 10.10.0.200'],
      removed: [],
    },
  ];

  return (
    <div className="space-y-6">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="bg-white/10 backdrop-blur-md rounded-2xl border border-white/20 p-6"
      >
        <h2 className="text-2xl font-bold text-white mb-2">Dry Run Preview</h2>
        <p className="text-purple-200">See exactly what would change before deployment</p>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="bg-blue-500/20 backdrop-blur-md rounded-xl border border-blue-400/30 p-4 flex items-center gap-3"
      >
        <CheckCircle size={24} className="text-blue-400 flex-shrink-0" />
        <div>
          <p className="text-white font-semibold">All devices compatible</p>
          <p className="text-blue-200 text-sm">All devices meet the minimum OS version requirement</p>
        </div>
      </motion.div>

      <div className="space-y-4">
        {dryRunData.map((item, idx) => (
          <motion.div
            key={item.device}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 + idx * 0.1 }}
            className="bg-white/10 backdrop-blur-md rounded-xl border border-white/20 p-6 space-y-4"
          >
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-xl font-bold text-white">{item.device}</h3>
                <p className="text-purple-300 text-sm">
                  <span className="inline-block px-3 py-1 bg-green-500/30 rounded-full text-green-200 text-xs font-semibold mr-2">
                    {item.stage}
                  </span>
                  OS: {item.os}
                </p>
              </div>
              <motion.div
                animate={{ scale: [1, 1.1, 1] }}
                transition={{ duration: 2, repeat: Infinity }}
                className={`w-12 h-12 rounded-full flex items-center justify-center ${
                  item.compatible ? 'bg-green-500/20 border-green-400' : 'bg-red-500/20 border-red-400'
                } border-2`}
              >
                {item.compatible ? (
                  <CheckCircle size={24} className="text-green-400" />
                ) : (
                  <AlertTriangle size={24} className="text-red-400" />
                )}
              </motion.div>
            </div>

            {item.added.length > 0 && (
              <div className="space-y-2">
                <h4 className="flex items-center gap-2 text-green-300 font-semibold">
                  <Plus size={18} />
                  Lines to Add
                </h4>
                <div className="ml-6 space-y-1">
                  {item.added.map((line, i) => (
                    <motion.div
                      key={i}
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.3 + i * 0.05 }}
                      className="bg-green-500/20 border border-green-400/30 rounded p-2 font-mono text-sm text-green-200"
                    >
                      + {line}
                    </motion.div>
                  ))}
                </div>
              </div>
            )}

            {item.removed.length > 0 && (
              <div className="space-y-2">
                <h4 className="flex items-center gap-2 text-red-300 font-semibold">
                  <Minus size={18} />
                  Lines to Remove
                </h4>
                <div className="ml-6 space-y-1">
                  {item.removed.map((line, i) => (
                    <motion.div
                      key={i}
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.3 + i * 0.05 }}
                      className="bg-red-500/20 border border-red-400/30 rounded p-2 font-mono text-sm text-red-200"
                    >
                      - {line}
                    </motion.div>
                  ))}
                </div>
              </div>
            )}
          </motion.div>
        ))}
      </div>

      <motion.button
        whileHover={{ scale: 1.05 }}
        whileTap={{ scale: 0.95 }}
        className="w-full py-3 bg-gradient-to-r from-green-500 to-emerald-500 text-white rounded-lg font-bold hover:shadow-lg hover:shadow-green-500/50 transition-all"
      >
        Deploy This Change
      </motion.button>
    </div>
  );
}