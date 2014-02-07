#define BUILDING_NODE_EXTENSION
#include <node.h>
#include <BallTrackerModuleExt.h>

using namespace v8;
using namespace rur;

void RegisterModule(Handle<Object> exports) {
  BallTrackerModuleExt::NodeRegister(exports);
}

NODE_MODULE(BallTrackerModule, RegisterModule)
