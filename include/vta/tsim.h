#ifndef VTA_DPI_TSIM_H_
#define VTA_DPI_TSIM_H_

#include <stdint.h>

#include "runtime/c_runtime_api.h"
#include "svdpi.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef unsigned char dpi8_t;

typedef unsigned short dpi16_t;

typedef unsigned int dpi32_t;

typedef unsigned long long dpi64_t;

/*! \brief the context handle */
typedef void *VTAContextHandle;

typedef void (*VTASimDPIFunc)(VTAContextHandle self, dpi8_t *wait,
                              dpi8_t *exit);

/*!
 * \brief Host DPI callback function that is invoked in VTAHostDPI.v every clock
 * cycle \param req_valid Host has a valid request for read or write a register
 * in Accel \param req_opcode Host request type, opcode=0 for read and opcode=1
 * for write \param req_addr Host request register address \param req_value Host
 * request value to be written to a register \param req_deq Accel is ready to
 * dequeue Host request \param resp_valid Accel has a valid response for Host
 * \param resp_value Accel response value for Host
 * \return 0 if success,
 */
typedef void (*VTAHostDPIFunc)(VTAContextHandle self, dpi8_t *req_valid,
                               dpi8_t *req_opcode, dpi16_t *req_addr,
                               dpi32_t *req_value, dpi8_t req_deq,
                               dpi8_t resp_valid, dpi32_t resp_value);

/*!
 * \brief Memory DPI callback function that is invoked in VTAMemDPI.v every
 * clock cycle \param req_valid Accel has a valid request for Host \param
 * req_opcode Accel request type, opcode=0 (read) and opcode=1 (write) \param
 * req_len Accel request length of size 8-byte and starts at 0 \param req_addr
 * Accel request base address \param wr_valid Accel has a valid value for Host
 * \param wr_value Accel has a value to be written Host
 * \param rd_valid Host has a valid value for Accel
 * \param rd_value Host has a value to be read by Accel
 */
typedef void (*VTAMemDPIFunc)(VTAContextHandle self, dpi8_t req_valid,
                              dpi8_t req_opcode, dpi8_t req_len,
                              dpi64_t req_addr, dpi8_t wr_valid,
                              const svLogicVecVal* wr_value, dpi8_t *rd_valid,
                              svLogicVecVal *rd_value, dpi8_t rd_ready);

/*! \brief The type of VTADPIInit function pointer */
typedef void (*VTADPIInitFunc)(VTAContextHandle handle, VTASimDPIFunc sim_dpi,
                               VTAHostDPIFunc host_dpi, VTAMemDPIFunc mem_dpi);

/*! \brief The type of VTADPISim function pointer */
typedef int (*VTADPISimFunc)();

/*!
 * \brief Set Host and Memory DPI functions
 * \param handle DPI Context handle
 * \param sim_dpi Sim DPI function
 * \param host_dpi Host DPI function
 * \param mem_dpi Memory DPI function
 */
TVM_DLL void VTADPIInit(VTAContextHandle handle, VTASimDPIFunc sim_dpi,
                        VTAHostDPIFunc host_dpi, VTAMemDPIFunc mem_dpi);

/*! \brief VTA hardware simulation thread */
TVM_DLL int VTADPISim();

#ifdef __cplusplus
}
#endif
#endif  // VTA_DPI_TSIM_H_
